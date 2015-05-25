package hoggaster.prices;

import static reactor.spring.context.annotation.SelectorType.REGEX;

import java.util.concurrent.atomic.LongAdder;

import hoggaster.oanda.responses.OandaPrice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.spring.context.annotation.Consumer;
import reactor.spring.context.annotation.Selector;
import reactor.spring.context.annotation.SelectorType;

/**
 * Consumes events with new {@link OandaPrice} info and stores them in the db.
 */
@Consumer
public class PriceRecorder {
	private static final Logger LOG = LoggerFactory.getLogger(PriceRecorder.class);
	
	private final PriceRepo priceRepo;
	
	private final LongAdder counter = new LongAdder();
	

	@Autowired
	public PriceRecorder(PriceRepo priceRepo, @Qualifier("priceEventBus") EventBus eventBus) {
		this.priceRepo = priceRepo;
	}
	
	@Selector(value="prices.(.+)",type=REGEX, eventBus="@priceEventBus")
	public void handleNewPrice(Event<OandaPrice> evt) {
		counter.increment();
		LOG.info("Count: " + counter.longValue());
	}

}
