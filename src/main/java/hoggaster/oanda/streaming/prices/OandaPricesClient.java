package hoggaster.oanda.streaming.prices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.TickContainer;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static hoggaster.domain.brokers.Broker.OANDA;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONNECTION;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

/**
 * Created by svante2 on 2016-03-03.
 */
@Component
public class OandaPricesClient {

    private final RestTemplate oandaClient;

    private final OandaProperties oandaProps;

    private final OandaResourcesProperties resources;

    private final HttpEntity<String> defaultHttpEntity;

    private final BrokerConnection oanda;

    private final EventBus pricesEventBus;

    private static final Logger LOG = LoggerFactory.getLogger(OandaPricesClient.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public OandaPricesClient(@Qualifier("oandaClient") RestTemplate oandaClient, @Qualifier("OandaBrokerConnection") BrokerConnection oanda, OandaProperties oandaProps, OandaResourcesProperties resources, @Qualifier("priceEventBus") EventBus pricesEventBus) {
        this.oandaClient = oandaClient;
        this.oandaProps = oandaProps;
        this.oanda = oanda;
        this.resources = resources;
        this.pricesEventBus = pricesEventBus;

        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.set(ACCEPT_ENCODING, "gzip, deflate");
        defaultHeaders.set(CONNECTION, "Keep-Alive");
        defaultHeaders.set(AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
        defaultHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        defaultHttpEntity = new HttpEntity<>(defaultHeaders);
    }

    @PostConstruct
    public void start() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getStreamingPrices());
        StringBuilder sb = new StringBuilder();
        List<CurrencyPair> cps = Lists.newArrayList(Arrays.asList(CurrencyPair.MAJORS));
        cps.addAll(Arrays.asList(CurrencyPair.MINORS));
        cps.addAll(Arrays.asList(CurrencyPair.EXOTICS));
        cps.forEach(cp -> sb.append(cp).append(","));

        final URI uri = builder.buildAndExpand(oandaProps.getMainAccountId(), sb.toString()).toUri();
        LOG.info("Using uri {}", uri.toString());
        Environment.get().getTimer().submit(l -> {
            oandaClient.execute(uri, HttpMethod.GET, request -> {
                HttpHeaders headers = request.getHeaders();
                headers.set(ACCEPT_ENCODING, "gzip, deflate");
                headers.set(CONNECTION, "Keep-Alive");
                headers.set(AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
                headers.setContentType(APPLICATION_FORM_URLENCODED);
            }, (ResponseExtractor<Object>) r -> {
                ClientHttpResponse response = r;
                InputStream stream = response.getBody();
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while((line = br.readLine()) != null) {
                    if(line.startsWith("{\"tick\"")) {
                        try {
                            final TickContainer container = objectMapper.readValue(line, TickContainer.class);
                            pricesEventBus.notify("prices." + container.tick.instrument, Event.wrap(new Price(
                                    container.tick.instrument, container.tick.bid, container.tick.ask, Instant.ofEpochMilli(container.tick.time.getTime()), OANDA)));
                        } catch (Exception e) {
                            LOG.error("Failed...", e);
                        }
                    } else if(line.startsWith("{\"heartbeat\"")) {
                        LOG.info("Got a heartbeat");
                    }
                }
                return r;
            });
        },10, SECONDS);

    }
}
