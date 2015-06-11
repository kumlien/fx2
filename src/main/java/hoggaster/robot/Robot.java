package hoggaster.robot;

import static hoggaster.robot.RobotStatus.RUNNING;
import static reactor.bus.selector.Selectors.R;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.oanda.responses.OandaMidPointCandle;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.prices.Price;
import hoggaster.rules.Condition;
import hoggaster.user.Depot;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.easyrules.core.AnnotatedRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;

/*
 * / Definition of code parameters
 DEFPARAM CumulateOrders = False // Cumulating positions deactivated

 // Conditions to enter long positions
 indicator1 = close
 indicator2 = Average[200](close)
 c1 = (indicator1 >= indicator2)

 indicator3 = RSI[2](close) + RSI[2](close[1])
 c2 = (indicator3 <= 10)

 IF c1 AND c2 THEN
 BUY (100000 + (strategyprofit))/((pipvalue/pipsize)*close) CONTRACT AT MARKET
 ENDIF

 // Conditions to exit long positions
 indicator4 = close
 indicator5 = Average[3](close)
 c3 = (indicator4 >= indicator5)

 IF c3 THEN
 SELL  AT MARKET
 ENDIF

 // Stops and targets
 */

//spring bean or basic object?
public class Robot implements Consumer<Event<?>> {

	private static final Logger LOG = LoggerFactory.getLogger(Robot.class);

	public final String id;

	public final String name;

	private final Instrument instrument;

	private final Set<Condition> buyConditions;

	private final Set<Condition> sellConditions;

	private final MovingAverageServiceImpl maService;

	private final Depot depot;

	private final EventBus priceEventBus;

	// The external broker this robot is connected to.
	private final Broker broker;

	private RobotStatus status = RobotStatus.UNKNOWN;

	private final AnnotatedRulesEngine annotatedRulesEngine = new AnnotatedRulesEngine();

	// Map with key = type of registration (prices, candles...) and registration
	// key as value.
	public final Map<String, Registration<?>> eventBusRegistrations = new ConcurrentHashMap<String, Registration<?>>();

	@Autowired
	public Robot(Depot depot, RobotDefinition definition,
			MovingAverageServiceImpl maService,
			@Qualifier("priceEventBus") EventBus priceEventBus, Broker broker) {
		Preconditions.checkArgument(priceEventBus != null,
				"The priceEventBus is null");
		Preconditions.checkArgument(maService != null,
				"The moving average service is null");
		Preconditions.checkArgument(depot != null, "The depot is null");
		Preconditions.checkArgument(definition != null,
				"The robot definition is null");
		Preconditions.checkArgument(definition.instrument != null,
				"The definition instrument is null");

		this.id = definition.getId(); // This is kind of wacky
		this.name = definition.name;
		this.instrument = definition.instrument;
		this.buyConditions = definition.getBuyConditions();
		this.sellConditions = definition.getSellConditions();
		this.maService = maService;
		this.depot = depot;
		this.priceEventBus = priceEventBus;
		this.broker = broker;

		buyConditions.stream().forEach(c -> {
			annotatedRulesEngine.registerRule(c);
		});

		sellConditions.stream().forEach(c -> {
			annotatedRulesEngine.registerRule(c);
		});
	}

	/**
	 * Kick the tires on this one.
	 * 
	 * Register for events.
	 */
	public void start() {
		Preconditions.checkState(priceEventBus != null,
				"The priceEventBus is null");
		Preconditions.checkState(instrument != null, "The instrument is null");
		eventBusRegistrations.put("priceRegistration",
				priceEventBus.on(R("prices." + instrument.name() + "*"), this));
		this.status = RobotStatus.RUNNING;
		LOG.info("Robot {} has started.", name);
	}

	/**
	 * Unregister for events.
	 */
	public void stop() {
		eventBusRegistrations.values().stream().forEach(Registration::cancel);
		this.status = RobotStatus.STOPPED;
		LOG.info("Robot {} has stopped.", name);
	}

	@Timed
	public void onNewPrice(Price price) {
		if (!isRunning()) {
			LOG.error("Ooops, not running but {}, bailing out", status);
			return;
		}

		LOG.debug("New price received: {}", price);
		if (!(price.instrument == instrument)) {
			LOG.debug(
					"Not to consider for buy since it's not for this robot: {} ",
					price.instrument);
			return;
		}
		LOG.info("Price is for us, let's see if any of the rules are based on price info (not candle info that is)");
		RobotExecutionContext ctx = new RobotExecutionContext(price, depot,
				instrument, maService);

		buyConditions.stream().forEach(c -> {
			c.setContext(ctx);
		});
		sellConditions.stream().forEach(c -> {
			c.setContext(ctx);
		});

		annotatedRulesEngine.fireRules();

		if (ctx.getPositiveBuyConditions().size() > 0) {
			LOG.info("Maybe we should buy something!");
			doBuy();
		} else if (ctx.getPositiveSellConditions().size() > 0) {
			LOG.info("Maybe we should sell something!");
			doSell();
		} else {
			LOG.info("No buy or sell actions triggered, seem like we should keep calm an carry on...");
		}

	}

	private void doSell() {
		if (!depot.ownThisInstrument(instrument)) {
			LOG.info("Nahh, we don't own {} yet...", instrument.name());
			return;
		}

		LOG.info("Ooops, we should sell what we got of {}!", instrument.name());
	}

	private void doBuy() {
		if (!depot.ownThisInstrument(instrument)) {
			LOG.info("Nahh, we allready own {}, only buy once...",
					instrument.name());
			return;
		}

		LOG.info("Ooops, we should buy since we don't own any {} yet!",
				instrument.name());
		OrderRequest order = new OrderRequest(depot.getBrokerId(), instrument,
				1000L, OrderSide.buy, OrderType.market, null, null);
		OandaOrderResponse response = broker.sendOrderToBroker(order);
		LOG.info("Order away! {}", response);
	}

	@Timed
	public void onNewCandle(OandaMidPointCandle candle) {

	}

	/**
	 * Accept an incoming event. TODO Synchronized?
	 * 
	 * @param t
	 */
	@Override
	public void accept(Event<?> t) {
		synchronized (this) {
			LOG.info(
					"Robot {} dealing with instrument {} received a new event: {}",
					name, instrument, t.getData());
			if (t.getData() instanceof Price) {
				onNewPrice((Price) t.getData());
			} else if (t.getData() instanceof OandaMidPointCandle) {
				onNewCandle((OandaMidPointCandle) t.getData());
			}
		}
	}

	public Depot getDepot() {
		return depot;
	}

	public RobotStatus getStatus() {
		return status;
	}

	public boolean isRunning() {
		return status == RUNNING;
	}

}
