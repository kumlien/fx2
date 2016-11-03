package hoggaster.rules.conditions;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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
 * method will get called and we add ourselves to the enterTrade or exitTrade list depending
 * on our {@link TradeAction}
 *
 * @author svante
 */
@Rule(name = "TwoIndicatorRule", description = "A rule comparing two indicators using the given operator")
public class TwoIndicatorCondition implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(TwoIndicatorCondition.class);

    public final String name;
    public final Indicator firstIndicator;
    public final Indicator secondIndicator;
    public final Comparator comparator;
    public final Integer priority;
    public final TradeAction tradeAction;

    private transient RobotExecutionContext ctx;

    //The kind of events we should react on.
    private final Set<MarketUpdateType> eventTypes;

    /**
     * @param name A describing name for this condition
     * @param firstIndicator
     * @param secondIndicator
     * @param comparator The comparator to use when comparing firstIndicator and secondIndicator
     * @param priority
     * @param tradeAction
     * @param eventTypes
     */
    public TwoIndicatorCondition(String name, Indicator firstIndicator, Indicator secondIndicator, Comparator comparator, Integer priority, TradeAction tradeAction, MarketUpdateType... eventTypes) {
        this.name = name;
        this.firstIndicator = firstIndicator;
        this.secondIndicator = secondIndicator;
        this.comparator = comparator;
        this.priority = priority;
        this.tradeAction = tradeAction;
        this.eventTypes = Sets.newHashSet(eventTypes);
    }

    @org.easyrules.annotation.Condition
    public boolean when() {
        Preconditions.checkState(ctx != null, "The RobotExecutionContext must be set before evaluating this rule");
        if (!eventTypes.contains(ctx.marketUpdate.getType())) {
            LOG.info("Return false since we only react on {} but this update was of type {}", eventTypes, ctx.marketUpdate.getType());
            return false;
        }
        Boolean result = comparator.apply(firstIndicator.value(ctx), secondIndicator.value(ctx));
        LOG.info("Result of rule with indicators '{}', '{}' compared with operator '{}' was '{}' given context {}", firstIndicator, secondIndicator, comparator, result, ctx);
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
                .add("operator", comparator)
                .add("priority", priority)
                .add("tradeAction", tradeAction)
                .add("ctx", ctx)
                .add("eventTypes", eventTypes)
                .toString();
    }
}
