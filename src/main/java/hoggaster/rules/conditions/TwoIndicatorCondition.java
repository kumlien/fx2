package hoggaster.rules.conditions;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.robot.RobotExecutionContext;
import hoggaster.domain.trades.TradeAction;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.indicators.Indicator;
import org.easyrules.annotation.Action;
import org.easyrules.annotation.Priority;
import org.easyrules.annotation.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static hoggaster.domain.trades.TradeAction.OPEN;


/**
 * Generic rule comparing two {@link Indicator}s with a given {@link Comparator}
 * Evaluated in the {@link #when()} method. If positive then the {@link #then()}
 * method will get called and we tradeOpened ourselves to the sendOrder- or sell action depending
 * on our {@link OrderSide}
 *
 * @author svante
 */
@Rule(name = "TwoIndicatorRule", description = "A rule comparing two indicators using the given operator")
public class TwoIndicatorCondition implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(TwoIndicatorCondition.class);

    public final String name;
    public final Indicator firstIndicator;
    public final Indicator secondIndicator;
    public final Comparator operator;
    public final Integer priority;
    public final TradeAction tradeAction;

    public final OrderSide orderSide;
    private transient RobotExecutionContext ctx;

    //The kind of events we should react on.
    private final Set<MarketUpdateType> eventTypes;

    /**
     * @param name A describing name for this condition
     * @param firstIndicator
     * @param secondIndicator
     * @param operator The operator to use when comparing firstIndicator and secondIndicator
     * @param priority
     * @param tradeAction Is this condition for opening or closing of a position?
     * @param orderSide Should we go short or long?
     * @param eventTypes
     */
    //TODO This is too much info for a condition! Order side and action doesn't belong here!
    public TwoIndicatorCondition(String name, Indicator firstIndicator, Indicator secondIndicator, Comparator operator, Integer priority, TradeAction tradeAction, OrderSide orderSide, MarketUpdateType... eventTypes) {
        this.name = name;
        this.firstIndicator = firstIndicator;
        this.secondIndicator = secondIndicator;
        this.operator = operator;
        this.priority = priority;
        this.tradeAction = tradeAction;
        this.orderSide = orderSide;
        this.eventTypes = Sets.newHashSet(eventTypes);
    }

    @org.easyrules.annotation.Condition
    public boolean when() {
        Preconditions.checkState(ctx != null, "The RobotExecutionContext must be set before evaluating this rule");
        if (!eventTypes.contains(ctx.marketUpdate.getType())) {
            LOG.info("Return false since we only react on {} but this update was of type {}", eventTypes, ctx.marketUpdate.getType());
            return false;
        }
        Boolean result = operator.apply(firstIndicator.value(ctx), secondIndicator.value(ctx));
        LOG.info("Result of rule with indicators '{}', '{}' compared with operator '{}' was '{}' given context {}", firstIndicator, secondIndicator, operator, result, ctx);
        return result;
    }

    @Action
    public void then() {
        if (tradeAction == OPEN) {
            ctx.addPositiveOpenTradeCondition(this);
        } else {
            ctx.addPositiveCloseTradeAction(this);
        }
    }

    @Priority
    public int prio() {
        return priority;
    }

    @Override
    public void setContext(RobotExecutionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("firstIndicator", firstIndicator)
                .add("secondIndicator", secondIndicator)
                .add("operator", operator)
                .add("priority", priority)
                .add("tradeAction", tradeAction)
                .add("ctx", ctx)
                .add("eventTypes", eventTypes)
                .toString();
    }
}
