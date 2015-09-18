package hoggaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import hoggaster.domain.ErrorResponse;
import hoggaster.oanda.OandaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Configuration
public class HttpConfig {

    private static Logger LOG = LoggerFactory.getLogger(Application.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

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
                if (response.getBody() != null) {
                    String body = CharStreams.toString(new InputStreamReader(response.getBody()));
                    LOG.error("Received a {} from oanda with body {}", response.getRawStatusCode(), body);
                    ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.toString()); //TODO Own exception
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

}
