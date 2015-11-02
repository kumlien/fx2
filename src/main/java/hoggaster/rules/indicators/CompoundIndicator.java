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
    public BigDecimal value(RobotExecutionContext ctx) {
        BigDecimal first = firstIndicator.value(ctx);
        BigDecimal second = secondIndicator.value(ctx);
        switch (operator) {
            case ADD:
                return first.add(second);
            case DIVIDE:
                return first.divide(second);
            case MULTIPLY:
                return first.multiply(second);
            case SUBTRACT:
                return first.subtract(second);
            default:
                throw new RuntimeException("wtf...: " + operator);
        }
    }

    public enum Operator {
        ADD, SUBTRACT, DIVIDE, MULTIPLY;
    }

}
