package hoggaster;

import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.common.collect.Lists;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableMongoRepositories
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RestTemplate oandaClient(OandaProperties props, ClientHttpRequestFactory requestFactory) {
    	RestTemplate rt = new RestTemplate(requestFactory);
    	rt.getMessageConverters().add(0,new FormHttpMessageConverter());
    	rt.getMessageConverters().forEach(c -> System.out.println(c));
    	return rt;
    }
    
    @Bean
    public ObjectMapper objectMapper() {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.registerModule(new JSR310Module());
    	return mapper;
    }
    
    
    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OandaProperties props) {
    	
    	List<Header> defaultHeaders = Lists.newArrayList(
    			new BasicHeader("Accept-Encoding", "gzip, deflate"),
    			new BasicHeader("Content-type", MediaType.APPLICATION_FORM_URLENCODED.getType()));
    	
    	CloseableHttpClient httpClient = HttpClientBuilder.create()
    			.setMaxConnPerRoute(10)
    			.setDefaultHeaders(defaultHeaders)
    			.setRetryHandler(new StandardHttpRequestRetryHandler(2, true)).
    			build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return factory;
    }
    
    @Bean
    public OandaApi oandaApi(OandaProperties oandaProps, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
    	return new OandaApi(oandaProps, restTemplate, resources);
    }
}
