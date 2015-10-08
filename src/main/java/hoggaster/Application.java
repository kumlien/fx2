package hoggaster;

import hoggaster.domain.OrderService;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("books");
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OandaProperties props) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(10).setRetryHandler(new StandardHttpRequestRetryHandler(2, true)).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return factory;
    }

    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection oandaApi(OandaProperties oandaProps, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
        return new OandaApi(oandaProps, restTemplate, resources);
    }

    @Bean
    public OrderService OandaOrderService(@Qualifier("OandaBrokerConnection") BrokerConnection oandaApi) {
        return (OrderService) oandaApi;
    }
}
