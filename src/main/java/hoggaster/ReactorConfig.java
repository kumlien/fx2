package hoggaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.EventBus;
import reactor.core.dispatch.ThreadPoolExecutorDispatcher;
import reactor.core.dispatch.WorkQueueDispatcher;
import reactor.spring.context.config.EnableReactor;

@Configuration
@EnableReactor
public class ReactorConfig {

    public static final Logger LOG = LoggerFactory.getLogger(ReactorConfig.class);

    @Bean(name = "priceEventBus_old")
    EventBus createPriceEventBus(Environment environment) {
        return EventBus.create(environment, Environment.THREAD_POOL);
    }

    @Bean(name = "priceEventBus")
    public EventBus priceEventBus() {
        return EventBus.config()
                .dispatcher(new ThreadPoolExecutorDispatcher(10,64,"priceEventBus"))
                .dispatchErrorHandler(t -> LOG.error("Error dispatching price event ", t))
                .uncaughtErrorHandler(t -> LOG.error("Uncaught error dispatching price event ", t))
                .get();
    }

    @Bean(name = "candleEventBus")
    EventBus createCandleEventBus(Environment environment) {
        return EventBus.create(environment, Environment.THREAD_POOL);
    }

    @Bean(name = "depotEventBus")
    public EventBus depotEventBus() {
        return EventBus.config()
                // WorkQueueDispatcher uses a blocking wait strategy
                .dispatcher(new WorkQueueDispatcher("depotEventBus", 10, 64,
                        t -> {
                            if (t instanceof InterruptedException) {
                                Thread.currentThread().interrupt();
                            } else {
                                LOG.error("Uncaught error dispatching depot event ", t);
                            }
                        }))
                .dispatchErrorHandler(t -> LOG.error("Error dispatching depot event ", t))
                .uncaughtErrorHandler(t -> LOG.error("Uncaught error dispatching depot event ", t))
                .get();
    }
}
