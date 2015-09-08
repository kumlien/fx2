package hoggaster.rules.conditions;

import static hoggaster.rules.conditions.BuyOrSell.BUY;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Condition;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.Operator;
import hoggaster.rules.indicators.Indicator;

import java.util.Set;

import org.easyrules.annotation.Action;
import org.easyrules.annotation.Priority;
import org.easyrules.annotation.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Generic rule comparing two {@link Indicator}s with a given {@link Operator}
 * Evaluated in the {@link #when()} method. If positive then the {@link #then()} 
 * method will get called and we add ourselves to the buy- or sell action depending
 * on our {@link BuyOrSell} 
 * 
 * @author svante
 */
@Rule(name = "TwoIndicatorRule", description = "A rule comparing two indicators using the given operator")
public class TwoIndicatorCondition implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(TwoIndicatorCondition.class);

    public final String name;
    public final Indicator firstIndicator;
    public final Indicator secondIndicator;
    public final Operator operator;
    public final Integer priority;
    public final BuyOrSell buyOrSell;
    private transient RobotExecutionContext ctx;

    //The kind of events we should react on.
    private final Set<MarketUpdateType> eventTypes;

    /**
     * @param name
     * @param firstIndicator
     * @param secondIndicator
     * @param operator
     * @param priority
     * @param buyOrSell
     * @param eventTypes
     */
    public TwoIndicatorCondition(String name, Indicator firstIndicator, Indicator secondIndicator, Operator operator, Integer priority, BuyOrSell buyOrSell, MarketUpdateType... eventTypes) {
	this.name = name;
	this.firstIndicator = firstIndicator;
	this.secondIndicator = secondIndicator;
	this.operator = operator;
	this.priority = priority;
	this.buyOrSell = buyOrSell;
	this.eventTypes = Sets.newHashSet(eventTypes);
    }

    @org.easyrules.annotation.Condition
    public boolean when() {
	Preconditions.checkState(ctx != null, "The RobotExecutionContext must be set before evaluating this rule");
	if(!eventTypes.contains(ctx.marketUpdate.getType())) {
	    LOG.info("Return false since we only react on {} but this update was of type {}", eventTypes, ctx.marketUpdate.getType());
	    return false;
	}
	Boolean result = operator.apply(firstIndicator.value(ctx), secondIndicator.value(ctx));
	LOG.info("Result of rule with indicators '{}', '{}' compared with operator '{}' was '{}' given context {}", firstIndicator, secondIndicator, operator, result, ctx);
	return result;
    }

    @Action
    public void then() {
	if (buyOrSell == BUY) {
	    ctx.addBuyAction(this);
	} else {
	    ctx.addSellAction(this);
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
	StringBuilder builder = new StringBuilder();
	builder.append("TwoIndicatorCondition [name=").append(name).append(", firstIndicator=").append(firstIndicator).append(", secondIndicator=").append(secondIndicator).append(", operator=").append(operator).append(", priority=").append(priority).append(", type=").append(buyOrSell).append("]");
	return builder.toString();
    }
}
