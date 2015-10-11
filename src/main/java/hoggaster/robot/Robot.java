package hoggaster.robot;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.depot.Depot;
import hoggaster.domain.Instrument;
import hoggaster.prices.Price;
import hoggaster.rules.Condition;
import hoggaster.talib.TALibService;
import org.easyrules.api.RulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.fn.Consumer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static hoggaster.robot.RobotStatus.RUNNING;
import static reactor.bus.selector.Selectors.R;

/*
 * / Definition of code parameters
 DEFPARAM CumulateOrders = False // Cumulating positions deactivated

 // Conditions to enter long positions
 indicator1 = close
 indicator2 = Average[200](close)
 c1 = (indicator1 >= indicator2)

 Dagens rsi[2] + gårdagens ska vara mindre än 11.
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

    // The buy conditions. Must be annotated with @Rule
    private final Set<Condition> buyConditions;

    // The sell conditions. Must be annotated with @Rule
    private final Set<Condition> sellConditions;

    private final TALibService taLibService;

    private final CandleService candleService;

    private final Depot depot;

    private final EventBus priceEventBus;

    private RobotStatus status = RobotStatus.UNKNOWN;

    private final RulesEngine rulesEngine;

    // Map with key = type of registration (prices, candles...) and registration
    // key as value.
    public final Map<String, Registration<?, ?>> eventBusRegistrations = new ConcurrentHashMap<String, Registration<?, ?>>();

    public Robot(Depot depot, RobotDefinition definition, EventBus priceEventBus, RulesEngine rulesEngine, TALibService taLibService, CandleService candleService) {
        Preconditions.checkArgument(priceEventBus != null, "The priceEventBus is null");
        Preconditions.checkArgument(depot != null, "The dbDepot is null");
        Preconditions.checkArgument(definition != null, "The robot definition is null");
        Preconditions.checkArgument(definition.instrument != null, "The definition instrument is null");
        Preconditions.checkArgument(rulesEngine != null, "The rule engine is null");
        Preconditions.checkArgument(taLibService != null, "The ta-lib service is null");
        Preconditions.checkArgument(candleService != null, "The bidAskCandleService is null");

        this.id = definition.getId(); // This is kind of wacky
        this.name = definition.name;
        this.instrument = definition.instrument;
        this.buyConditions = definition.getBuyConditions();
        this.sellConditions = definition.getSellConditions();
        this.depot = depot;
        this.priceEventBus = priceEventBus;
        this.rulesEngine = rulesEngine;
        this.taLibService = taLibService;
        this.candleService = candleService;

        buyConditions.stream().forEach(c -> {
            rulesEngine.registerRule(c);
        });

        sellConditions.stream().forEach(c -> {
            rulesEngine.registerRule(c);
        });
    }

    /**
     * Kick the tires on this one.
     * <p>
     * Register for events.
     */
    public void start() {
        LOG.info("This is Robot {} starting up", this);
        Registration<Object, Consumer<? extends Event<?>>> reg = priceEventBus.on(R("prices." + instrument.name() + "*"), this);
        eventBusRegistrations.put("priceRegistration", reg);
        this.status = RobotStatus.RUNNING;
        LOG.info("Robot {} has started.", name);
    }

    /**
     * Unregister for events.
     */
    public void stop() {
        this.status = RobotStatus.STOPPED;
        eventBusRegistrations.values().stream().forEach(Registration::cancel);
        LOG.info("Robot {} has stopped.", name);
    }

    /*
     * Handle new incoming price
     */
    @Timed
    private void onNewPrice(Price price) {
        LOG.info("Price is for us, let's see if any of the rules are based on price info (not candle info that is)");
        RobotExecutionContext ctx = new RobotExecutionContext(price, instrument, taLibService, candleService);
        setCtxOnConditions(ctx);
        rulesEngine.fireRules();

        //All buy conditions must be positive
        if (ctx.getPositiveBuyConditions().size() == buyConditions.size()) {
            LOG.info("Maybe we should buy something based on new price!");
            doBuy();
        } else if (ctx.getPositiveSellConditions().size() > 0) { //Enough with one positive sell condition
            LOG.info("Maybe we should sell something based on new price!");
            doSell();
        } else {
            LOG.info("No buy or sell actions triggered, seem like we should keep calm an carry on...");
        }

    }

    /*
     * Handle new incoming candle.
     */
    @Timed
    private void onNewCandle(Candle candle) {
        LOG.info("Candle is for us, let's see if any of the rules are based on price info (not candle info that is)");
        RobotExecutionContext ctx = new RobotExecutionContext(candle, instrument, taLibService, candleService);
        setCtxOnConditions(ctx);
        rulesEngine.fireRules();

        if (ctx.getPositiveBuyConditions().size() == buyConditions.size()) {
            doBuy();
        } else if (ctx.getPositiveSellConditions().size() > 0) {
            doSell();
        } else {
            LOG.info("No buy or sell actions triggered, seem like we should keep calm an carry on...");
        }

    }

    private void setCtxOnConditions(RobotExecutionContext ctx) {
        buyConditions.parallelStream().forEach(c -> {
            c.setContext(ctx);
        });
        sellConditions.parallelStream().forEach(c -> {
            c.setContext(ctx);
        });
    }

    private void doSell() {
        depot.sell(instrument, this.id);
//        if (!depot.ownThisInstrument(instrument)) {
//            LOG.info("Nahh, we don't own {} yet...", instrument.name());
//            return;
//        }
//
//        LOG.info("Ooops, we should sell what we got of {}!", instrument.name());
    }

    private void doBuy() {
        depot.buy(instrument, this.id);
//        if (dbDepot.ownThisInstrument(instrument)) {
//            LOG.info("Nahh, we already own {}, only buy once...", instrument.name());
//            return;
//        }
//
//        LOG.info("Ooops, we should buy since we don't own any {} yet!", instrument.name());
//        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), instrument, 1000L, OrderSide.buy, OrderType.market, null, null);
//        OandaOrderResponse response = orderService.sendOrder(order);
//        LOG.info("Order away and we got an response! {}", response);
    }

    /**
     * Accept an incoming event.
     *
     * @param t
     */
    @Override
    public void accept(Event<?> t) {
        if (!isRunning()) {
            LOG.warn("Ooops, not running but {}, bailing out", status);
            return;
        }
        Preconditions.checkArgument(t != null, "The event is not allowed to be null");
        Preconditions.checkArgument(t.getData() != null, "The event payload is not allowed to be null");
        synchronized (this) {
            LOG.info("Robot {} dealing with instrument {} received a new event: {}", name, instrument, t.getData());
            if (t.getData() instanceof Price) {
                if (!(((Price) t.getData()).instrument == instrument)) {
                    LOG.debug("Not to consider since it's not for this robot: {} ", ((Price) t.getData()).instrument);
                    return;
                }
                onNewPrice((Price) t.getData());
            } else if (t.getData() instanceof Candle) {
                if (!(((Candle) t.getData()).instrument == instrument)) {
                    LOG.debug("Not to consider since it's not for this robot: {} ", ((Candle) t.getData()).instrument);
                    return;
                }
                onNewCandle((Candle) t.getData());
            }
        }
    }

    public RobotStatus getStatus() {
        return status;
    }

    public boolean isRunning() {
        return status == RUNNING;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Robot [id=").append(id).append(", name=").append(name).append(", instrument=").append(instrument).append(", buyConditions=").append(buyConditions).append(", sellConditions=").append(sellConditions).append(", dbDepot=").append(depot).append(", status=").append(status).append("]");
        return builder.toString();
    }

}
