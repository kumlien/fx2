package hoggaster.domain.prices;

import hoggaster.oanda.responses.OandaPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.spring.context.annotation.Consumer;
import reactor.spring.context.annotation.Selector;

import java.util.concurrent.atomic.LongAdder;

import static reactor.spring.context.annotation.SelectorType.REGEX;

/**
 * Consumes events with new {@link OandaPrice} info and stores them in the db.
 */
@Consumer
public class PriceRecorder {
    private static final Logger LOG = LoggerFactory.getLogger(PriceRecorder.class);

    private final PriceService priceService;

    private final LongAdder counter = new LongAdder();

    @Autowired
    public PriceRecorder(PriceService priceService, @Qualifier("priceEventBus") EventBus eventBus) {
        this.priceService = priceService;
    }

    @Selector(value = "prices.(.+)", type = REGEX, eventBus = "@priceEventBus")
    public void handleNewPrice(Event<Price> evt) {
        LOG.debug("Got a price event: {}", evt.getData());
        priceService.store(evt.getData());
    }
}
