package hoggaster.domain.robot;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.prices.Price;
import hoggaster.rules.conditions.Condition;
import hoggaster.talib.TALibService;
import org.easyrules.api.RulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.fn.Consumer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static hoggaster.domain.robot.RobotStatus.RUNNING;
import static hoggaster.domain.robot.RobotStatus.STOPPED;
import static reactor.bus.selector.Selectors.R;

/*
 * / Definition of code parameters
 DEFPARAM CumulateOrders = False // Cumulating positions deactivated

// Conditions to enter long positions
indicator1 = close
indicator2 = Average[200](close)
indicator3 = Williams[2](close)
indicator4 = average[d](close)

c1 = (indicator1 >= indicator2)
c2 = (indicator3 <= -100+r)

IF c1 AND c2 THEN
BUY 1 CONTRACT AT MARKET
ENDIF

// Conditions to exit long positions
c3 = (indicator1 >= indicator4)

IF c3 THEN
SELL  AT MARKET
ENDIF

// Conditions to enter short positions
c4 = (indicator1 < indicator2)
c5 = (indicator3 >= 0-r)

IF c4 AND c5 THEN
SELLSHORT 1 CONTRACT AT MARKET
ENDIF

// Conditions to exit short positions
c6 = (indicator1 <= indicator4)

IF c6 THEN
EXITSHORT  AT MARKET
ENDIF

// Stops and targets
 */

/**
 * The guy which reacts to new prices or candles, evaluates it's conditions to see if it should open/close a trade based on the input.
 */
public class Robot implements Consumer<Event<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(Robot.class);
    public static final BigDecimal PART_OF_AVAILABLE_MARGIN = new BigDecimal("0.02");

    public final String id;

    public final String name;

    private final CurrencyPair currencyPair;

    // The buy conditions. Must be annotated with @Rule
    private final Set<Condition> openTradeConditions;

    // The sell conditions. Must be annotated with @Rule
    private final Set<Condition> closeTradeConditions;

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
        Preconditions.checkArgument(definition.currencyPair != null, "The definition currencyPair is null");
        Preconditions.checkArgument(rulesEngine != null, "The rule engine is null");
        Preconditions.checkArgument(taLibService != null, "The ta-lib service is null");
        Preconditions.checkArgument(candleService != null, "The bidAskCandleService is null");

        this.id = definition.getId(); // This is kind of wacky
        this.name = definition.name;
        this.currencyPair = definition.currencyPair;
        this.openTradeConditions = definition.getEnterConditions();
        this.closeTradeConditions = definition.getExitConditions();
        this.depot = depot;
        this.priceEventBus = priceEventBus;
        this.rulesEngine = rulesEngine;
        this.taLibService = taLibService;
        this.candleService = candleService;

        openTradeConditions.forEach(rulesEngine::registerRule);
        closeTradeConditions.forEach(rulesEngine::registerRule);
    }

    /**
     * Kick the tires on this one.
     * <p>
     * Register for events.
     */
    public void start() {
        LOG.info("This is Robot {} starting up", this);
        Registration<Object, Consumer<? extends Event<?>>> reg = priceEventBus.on(R("prices." + currencyPair.name() + "*"), this);
        eventBusRegistrations.put("priceRegistration", reg);
        this.status = RUNNING;
        LOG.info("Robot {} has started.", name);
    }

    /**
     * Unregister for events.
     */
    public void stop() {
        this.status = STOPPED;
        eventBusRegistrations.values().stream().forEach(Registration::cancel);
        LOG.info("Robot {} has stopped.", name);
    }

    /*
     * Handle new incoming price
     */
    @Timed
    private void onNewPrice(Price price) {
        LOG.info("Price is for us, let's see if any of the rules are based on price info (not candle info that is)");
        RobotExecutionContext ctx = new RobotExecutionContext(price, currencyPair, taLibService, candleService);
        setCtxOnConditions(ctx);
        rulesEngine.fireRules();

        //TODO Check this one!!! not correct. We need the order side too
        if (ctx.getPositiveOpenTradeConditions().size() == openTradeConditions.size()) {
            LOG.info("Maybe we should enter a buy trade something based on new price!");
            askDepotToOpenTrade(price, OrderSide.buy);
        } else if (ctx.getPositiveCloseTradeConditions().size() > 0) {
            LOG.info("Maybe we should enter a sell trade based on new price!");
            askDepotToOpenTrade(price, OrderSide.sell);
        } else {
            LOG.info("No sendOrder or sell actions triggered, seem like we should keep calm an carry on...");
        }

    }

    /*
     * Handle new incoming candle.
     */
    @Timed
    private void onNewCandle(Candle candle) {
        LOG.info("Candle is for us, let's see if any of the rules are based on price info (not candle info that is)");
        RobotExecutionContext ctx = new RobotExecutionContext(candle, currencyPair, taLibService, candleService);
        setCtxOnConditions(ctx);
        rulesEngine.fireRules();

        if (ctx.getPositiveOpenTradeConditions().size() == openTradeConditions.size()) { //All openTrade conditions say open trade! TODO refactor with compound conditions
            askDepotToOpenTrade(candle, OrderSide.buy);
        } else if (ctx.getPositiveCloseTradeConditions().size() > 0) { //At least one sell condition say sell!
            doCloseTrade();
        } else {
            LOG.info("No sendOrder or sell actions triggered, seem like we should keep calm an carry on...");
        }

    }

    private void setCtxOnConditions(RobotExecutionContext ctx) {
        openTradeConditions.forEach(c -> {
            c.setContext(ctx);
        });
        closeTradeConditions.forEach(c -> {
            c.setContext(ctx);
        });
    }

    private void doCloseTrade() {
        depot.closeTrade(currencyPair, this.id);
    }

    //TODO Read sendOrder percentage from db, hard code 2% for now
    private void askDepotToOpenTrade(MarketUpdate marketUpdate, OrderSide side) {
        depot.openTrade(currencyPair, side, PART_OF_AVAILABLE_MARGIN, marketUpdate, this.id);
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
            LOG.info("Robot {} dealing with currencyPair {} received a new event: {}", name, currencyPair, t.getData());
            if (t.getData() instanceof Price) {
                if (!(((Price) t.getData()).currencyPair == currencyPair)) {
                    LOG.debug("Not to consider since it's not for this robot: {} ", ((Price) t.getData()).currencyPair);
                    return;
                }
                onNewPrice((Price) t.getData());
            } else if (t.getData() instanceof Candle) {
                if (!(((Candle) t.getData()).currencyPair == currencyPair)) {
                    LOG.debug("Not to consider since it's not for this robot: {} ", ((Candle) t.getData()).currencyPair);
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
        builder.append("Robot [id=").append(id).append(", name=").append(name).append(", currencyPair=").append(currencyPair).append(", openTradeConditions=").append(openTradeConditions).append(", closeTradeConditions=").append(closeTradeConditions).append(", dbDepot=").append(depot).append(", status=").append(status).append("]");
        return builder.toString();
    }

}
