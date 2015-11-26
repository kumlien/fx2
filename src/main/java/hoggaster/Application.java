package hoggaster;

import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.orders.OrderService;
import hoggaster.oanda.OandaApi;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;

@SpringBootApplication
@EnableConfigurationProperties()
@EnableScheduling
@PropertySource("file:/data/fx2/config/application.yml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection oandaApi(OandaProperties oandaProps, RetryTemplate oandaRetryTemplate, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
        return new OandaApi(oandaProps, oandaRetryTemplate, restTemplate, resources);
    }

    @Bean
    public OrderService OandaOrderService(@Qualifier("OandaBrokerConnection") BrokerConnection oandaApi) {
        return (OrderService) oandaApi;
    }

}
