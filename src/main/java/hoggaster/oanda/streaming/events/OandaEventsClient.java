package hoggaster.oanda.streaming.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.prices.Price;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

/**
 * Created by svante2 on 2016-04-14
 */
@Component
public class OandaEventsClient {

    private final RestTemplate oandaClient;

    private final OandaProperties oandaProps;

    private final OandaResourcesProperties resources;

    private final BrokerConnection oanda;

    private final EventBus pricesEventBus;

    private static final Logger LOG = LoggerFactory.getLogger(OandaEventsClient.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public OandaEventsClient(@Qualifier("oandaClient") RestTemplate oandaClient, @Qualifier("OandaBrokerConnection") BrokerConnection oanda, OandaProperties oandaProps, OandaResourcesProperties resources, @Qualifier("priceEventBus") EventBus pricesEventBus) {
        this.oandaClient = oandaClient;
        this.oandaProps = oandaProps;
        this.oanda = oanda;
        this.resources = resources;
        this.pricesEventBus = pricesEventBus;
    }

    //A lot of things to do on this one... Check heartbeats, re-connect on failure etc
    @PostConstruct
    public void init() {
        //Environment.timer().submit(startTime -> startListenForEventsAsync(), 15, TimeUnit.SECONDS);
        LOG.info("Leaving init...");
    }


    public void startListenForEventsAsync() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getStreamingEvents());
        final URI uri = builder.build().toUri();
        LOG.info("Using uri {}", uri.toString());
        oandaClient.execute(uri, GET, request -> {
            HttpHeaders headers = request.getHeaders();
            headers.set(ACCEPT_ENCODING, "gzip, deflate");
            headers.set(CONNECTION, "Keep-Alive");
            headers.set(AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
            headers.setContentType(APPLICATION_FORM_URLENCODED);
        }, r -> fetchEventsAsync(r)
                .subscribe(
                        event -> {
                            LOG.info("got an event: {}", event);
                        },
                        err -> {
                            LOG.warn("Error streaming events:", err);
                            startListenForEventsAsync();
                        },
                        () -> {
                            LOG.info("Fetch events stream completed, start listen again...");
                            startListenForEventsAsync();
                        }));


    }

    private Observable<Transaction> fetchEventsAsync(ClientHttpResponse r) {
        LOG.info("In fetch events async...");
        Observable<Transaction> o = Observable.create((Observable.OnSubscribe<Transaction>) s -> {
            try {
                InputStream stream = r.getBody();
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null && !s.isUnsubscribed()) {
                    if (line.startsWith("{\"transaction\"")) {
                        s.onNext(parse(line));
                    } else if (line.startsWith("{\"heartbeat\"")) {
                        LOG.debug("Got a heartbeat");
                    }
                }
                s.onCompleted();
            } catch (Exception e) {
                s.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doAfterTerminate(r::close);

        return o;
    }

    private static Transaction parse(String response) throws IOException {
        final EventContainer container = objectMapper.readValue(response, EventContainer.class);
        return container.transaction;
    }

    private void sendTick(Price p) {
        try {
            LOG.debug("Got a tick {}", p);
            pricesEventBus.notify("prices." + p.currencyPair, Event.wrap(p));
        } catch (Exception e) {
            LOG.error("Failed...", e);
        }
    }
}
