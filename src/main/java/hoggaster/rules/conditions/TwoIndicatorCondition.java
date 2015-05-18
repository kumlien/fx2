package hoggaster.rules.conditions;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Condition;
import hoggaster.rules.Indicator;
import hoggaster.rules.Operator;

import org.easyrules.annotation.Action;
import org.easyrules.annotation.Priority;
import org.easyrules.annotation.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Generic rule comparing two {@link Indicator}s with a given {@link Operator}
 * 
 * @author svante
 */
@Rule(name="TwoIndicatorRule", description="A rule comparing two indicator with the given operator")
public class TwoIndicatorCondition implements Condition {
	
	private static final Logger LOG = LoggerFactory.getLogger(TwoIndicatorCondition.class);
	
	private final String name;
	private final Indicator firstIndicator;
	private final Indicator secondIndicator;
	private final Operator operator;
	private final Integer priority;
	private final ConditionType type;
	private transient RobotExecutionContext ctx;
	
	public TwoIndicatorCondition(String name, Indicator firstIndicator, Indicator secondIndicator, Operator operator, Integer priority, ConditionType type) {
		this.name = name;
		this.firstIndicator = firstIndicator;
		this.secondIndicator = secondIndicator;
		this.operator = operator;
		this.priority = priority;
		this.type = type;
	}

	
	@org.easyrules.annotation.Condition
	public boolean when() {
		Preconditions.checkState(ctx != null, "The RobotExecutionContext must be set before evaluating this rule");
		Boolean result = operator.apply(firstIndicator.value(ctx), secondIndicator.value(ctx));
		LOG.info("Result of rule with indicators '{}', '{}' compared with operator '{}' was '{}' given context {}", firstIndicator, secondIndicator, operator, result, ctx);
		return result;
	}
	
	@Action
	public void then() {
		if(type == ConditionType.BUY) {
			ctx.addBuyAction(this);
		} else {
			ctx.addSellAction(this);
		}
	}
	
	@Priority
	public int prio() {
		return priority;
	}


	public void setContext(RobotExecutionContext ctx) {
		this.ctx = ctx;
	}
}