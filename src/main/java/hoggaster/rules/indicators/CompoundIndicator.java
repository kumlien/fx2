package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;

import java.math.BigDecimal;

/**
 * Indicator which is based on two other indicators and an {@link Operator}
 */
public class CompoundIndicator implements Indicator {
    
    private final Indicator firstIndicator;
    
    private final Indicator secondIndicator;
    
    private final Operator operator;

    public CompoundIndicator(Indicator firstIndicator, Indicator secondIndicator, Operator operator) {
	this.firstIndicator = firstIndicator;
	this.secondIndicator = secondIndicator;
	this.operator = operator;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
	BigDecimal first = new BigDecimal(firstIndicator.value(ctx));
	BigDecimal second = new BigDecimal(secondIndicator.value(ctx));
	switch (operator) {
	    case ADD:
		return first.add(second).doubleValue();
	    case DIVIDE:
		return first.divide(second).doubleValue();
	    case MULTIPLY:
		return first.multiply(second).doubleValue();
	    case SUBTRACT:
		return first.subtract(second).doubleValue();
	    default:
		throw new RuntimeException("wtf...: " + operator);
	}
    }
    
    public enum Operator {
	ADD, SUBTRACT, DIVIDE, MULTIPLY;
    }

}
