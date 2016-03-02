package hoggaster.oanda.streaming.prices;

import hoggaster.oanda.OandaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import reactor.Environment;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

/**
 * Created by svante2 on 2016-03-03.
 */
@Component
public class OandaPricesClient {

    private final RestTemplate oandaClient;

    private final OandaProperties oandaProps;

    private final HttpEntity<String> defaultHttpEntity;

    private final String url = "https://stream-fxpractice.oanda.com/v1/prices?accountId=9678914&instruments=USD_SEK,EUR_USD,EUR_SEK";


    @Autowired
    public OandaPricesClient(@Qualifier("oandaClient") RestTemplate oandaClient, OandaProperties oandaProps) {
        this.oandaClient = oandaClient;
        this.oandaProps = oandaProps;
        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.set(ACCEPT_ENCODING, "gzip, deflate");
        defaultHeaders.set(CONNECTION, "Keep-Alive");
        defaultHeaders.set(AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
        defaultHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        defaultHttpEntity = new HttpEntity<>(defaultHeaders);
    }

    @PostConstruct
    public void start() {
        Environment.get().getTimer().submit(l -> {
            oandaClient.execute(url, HttpMethod.GET, request -> {
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
                    System.out.println("Got " + line);
                }
                return r;
            });
        },10, SECONDS);

    }
}
