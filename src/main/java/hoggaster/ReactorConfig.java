package hoggaster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;
import reactor.spring.context.config.EnableReactor;

@Configuration
@EnableReactor
public class ReactorConfig {

    @Bean(name = "priceEventBus")
    EventBus createPriceEventBus(Environment environment) {
	return EventBus.create(environment, Environment.THREAD_POOL);
    }
}
