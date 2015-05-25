package hoggaster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;
import reactor.spring.context.config.EnableReactor;

@Configuration
@EnableReactor
public class ReactorConfig {
	
//	@Bean
//	Environment reactorEnvironment() {
//		if(Environment.alive()) {
//			return Environment.get();
//		}
//		return Environment.initialize();
//	}
	
	@Bean
	reactor.core.Environment coreEnv() {
		return new reactor.core.Environment();
	}

	
	@Bean(name="priceEventBus")
    EventBus createPriceEventBus(Environment env) {
        return EventBus.create(env,Environment.THREAD_POOL);
    }
}
