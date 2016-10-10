package it;

import hoggaster.domain.brokers.BrokerConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.bus.EventBus;

import static org.mockito.Mockito.mock;

/**
 * Created by svante2 on 2016-10-05.
 */
@Configuration
public class ITConfiguration {

    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection brokerConnection() {
        return mock(BrokerConnection.class);
    }

    @Bean
    public EventBus depotEventBus() {
        return EventBus.create();
    }
}
