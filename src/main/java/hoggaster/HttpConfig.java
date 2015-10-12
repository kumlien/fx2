package hoggaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import hoggaster.domain.ErrorResponse;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.exceptions.RateLimitException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HttpConfig {

    public static final int BACK_OFF_PERIOD = 2000;
    public static final int MAX_ATTEMPTS = 3;
    public static final String OANDA_CALL_CTX_ATTR = "oanda.call";
    private static Logger LOG = LoggerFactory.getLogger(Application.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Bean(name = "oandaRetryTemplate")
    public RetryTemplate oandaRetryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(fixedBackoffPolicy());
        retryTemplate.registerListener(new LoggingRetryListener());
        return retryTemplate;
    }

    @Bean
    public SimpleRetryPolicy simpleRetryPolicy() {
        final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpServerErrorException.class, true);
        retryableExceptions.put(ResourceAccessException.class, true);
        retryableExceptions.put(HttpMessageNotReadableException.class, true);
        retryableExceptions.put(RateLimitException.class, true);
        return new SimpleRetryPolicy(MAX_ATTEMPTS, retryableExceptions);
    }

    @Bean
    public FixedBackOffPolicy fixedBackoffPolicy() {
        final FixedBackOffPolicy backoffPolicy = new FixedBackOffPolicy();
        backoffPolicy.setBackOffPeriod(BACK_OFF_PERIOD);
        return backoffPolicy;
    }

    private static class LoggingRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> boolean open(final RetryContext context, final RetryCallback<T, E> callback) {
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            if (throwable != null) {
                LOG.info("Final {} retry attempt failed, exception will be propagated.", context.getAttribute(OANDA_CALL_CTX_ATTR), throwable);
            }
        }

        @Override
        public <T, E extends Throwable> void onError(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            LOG.info("{} call failed with {}, retryCount is {}", context.getAttribute(OANDA_CALL_CTX_ATTR), throwable.getClass().getSimpleName(),
                    context.getRetryCount());
        }
    }

    @Bean(name="oandaClient")
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
                if (response.getBody() != null) {
                    String body = CharStreams.toString(new InputStreamReader(response.getBody()));
                    LOG.error("Received a {} from oanda with body {}", response.getRawStatusCode(), body);
                    ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
                    if(errorResponse.code == 68) {
                        throw new RateLimitException(errorResponse.message);
                    }
                    throw new RuntimeException(errorResponse.toString()); //TODO Own exception
                }
                super.handleError(response);
            }
        });
        return rt;
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OandaProperties props) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(10).setRetryHandler(new StandardHttpRequestRetryHandler(2, true)).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return factory;
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

}
