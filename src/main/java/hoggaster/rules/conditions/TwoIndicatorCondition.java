package hoggaster.rules.conditions;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Condition;
import hoggaster.rules.EventType;
import hoggaster.rules.Indicator;
import hoggaster.rules.Operator;

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
 * on our {@link ConditionType} 
 * 
 * @author svante
 */
@Rule(name = "TwoIndicatorRule", description = "A rule comparing two indicator with the given operator")
public class TwoIndicatorCondition implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(TwoIndicatorCondition.class);

    public final String name;
    public final Indicator firstIndicator;
    public final Indicator secondIndicator;
    public final Operator operator;
    public final Integer priority;
    public final ConditionType type;
    private transient RobotExecutionContext ctx;

    //The kind of events we should react on.
    private final Set<EventType> eventTypes;

    public TwoIndicatorCondition(String name, Indicator firstIndicator, Indicator secondIndicator, Operator operator, Integer priority, ConditionType type, EventType... eventTypes) {
	this.name = name;
	this.firstIndicator = firstIndicator;
	this.secondIndicator = secondIndicator;
	this.operator = operator;
	this.priority = priority;
	this.type = type;
	this.eventTypes = Sets.newHashSet(eventTypes);
    }

    @org.easyrules.annotation.Condition
    public boolean when() {
	Preconditions.checkState(ctx != null, "The RobotExecutionContext must be set before evaluating this rule");
	if(!eventTypes.contains(ctx.eventType)) {
	    LOG.info("Return false since we only react on {} but this event was of type {}", eventTypes, ctx.eventType);
	    return false;
	}
	Boolean result = operator.apply(firstIndicator.value(ctx), secondIndicator.value(ctx));
	LOG.info("Result of rule with indicators '{}', '{}' compared with operator '{}' was '{}' given context {}", firstIndicator, secondIndicator, operator, result, ctx);
	return result;
    }

    @Action
    public void then() {
	if (type == ConditionType.BUY) {
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
	builder.append("TwoIndicatorCondition [name=").append(name).append(", firstIndicator=").append(firstIndicator).append(", secondIndicator=").append(secondIndicator).append(", operator=").append(operator).append(", priority=").append(priority).append(", type=").append(type).append("]");
	return builder.toString();
    }
}
