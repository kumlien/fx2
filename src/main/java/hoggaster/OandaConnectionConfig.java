package hoggaster;

import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.orders.OrderService;
import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;

/**
 * Created by svante2 on 2015-11-26.
 */
@Configuration
public class OandaConnectionConfig {

    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection oandaApi(OandaProperties oandaProps, RetryTemplate oandaRetryTemplate, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
        return new OandaApi(oandaProps, oandaRetryTemplate, restTemplate, resources);
    }

    @Bean
    public OrderService OandaOrderService(@Qualifier("OandaBrokerConnection") BrokerConnection oandaApi) {
        return (OrderService) oandaApi;
    }
}
