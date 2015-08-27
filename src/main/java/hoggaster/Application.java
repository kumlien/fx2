package hoggaster;

import hoggaster.domain.BrokerConnection;
import hoggaster.domain.OrderService;
import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;

import java.io.UnsupportedEncodingException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RestTemplate oandaClient(OandaProperties props, ClientHttpRequestFactory requestFactory) {
    	RestTemplate rt = new RestTemplate(requestFactory);
    	rt.getMessageConverters().add(0,new FormHttpMessageConverter());
    	return rt;
    }
    
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("books");
    }
    
    
    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OandaProperties props) {
    	
    	CloseableHttpClient httpClient = HttpClientBuilder.create()
    			.setMaxConnPerRoute(10)
    			.setRetryHandler(new StandardHttpRequestRetryHandler(2, true)).
    			build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return factory;
    }
    
    @Bean(name="OandaBrokerConnection")
    public BrokerConnection oandaApi(OandaProperties oandaProps, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
    	return new OandaApi(oandaProps, restTemplate, resources);
    }
    
    @Bean
    public OrderService OandaOrderService(@Qualifier("OandaBrokerConnection") BrokerConnection oandaApi) {
	return (OrderService) oandaApi;
    }
}
