package hoggaster;

import hoggaster.domain.BrokerConnection;
import hoggaster.domain.ErrorResponse;
import hoggaster.domain.OrderService;
import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class Application {

    private static Logger LOG = LoggerFactory.getLogger(Application.class);
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
	SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate oandaClient(OandaProperties props, ClientHttpRequestFactory requestFactory) {
	RestTemplate rt = new RestTemplate(requestFactory);
	rt.getMessageConverters().add(0, new FormHttpMessageConverter());
	rt.setErrorHandler(new DefaultResponseErrorHandler() {

	    @Override
	    public boolean hasError(ClientHttpResponse response) throws IOException {
		return super.hasError(response);
	    }

	    @Override
	    public void handleError(ClientHttpResponse response) throws IOException {
		if (getHttpStatusCode(response).is4xxClientError() && response.getBody() != null) {
		    String body = CharStreams.toString(new InputStreamReader(response.getBody()));
		    LOG.error("Received a {} from oanda with body {}", response.getRawStatusCode(), body);
		    ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		    throw new RuntimeException(errorResponse.toString());
		}
		super.handleError(response);
	    }
	});
	return rt;
    }

    private HttpStatus getHttpStatusCode(ClientHttpResponse response) throws IOException {
	HttpStatus statusCode;
	try {
	    statusCode = response.getStatusCode();
	} catch (IllegalArgumentException ex) {
	    throw new UnknownHttpStatusCodeException(response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
	}
	return statusCode;
    }

    private Charset getCharset(ClientHttpResponse response) {
	HttpHeaders headers = response.getHeaders();
	MediaType contentType = headers.getContentType();
	return contentType != null ? contentType.getCharSet() : null;
    }

    private byte[] getResponseBody(ClientHttpResponse response) {
	try {
	    InputStream responseBody = response.getBody();
	    if (responseBody != null) {
		return FileCopyUtils.copyToByteArray(responseBody);
	    }
	} catch (IOException ex) {
	    // ignore
	}
	return new byte[0];
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
