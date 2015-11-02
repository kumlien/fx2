package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;

import java.math.BigDecimal;


/**
 * This interface represents some kind of indicator, a very basic indicator would
 * be the current price of an CurrencyPair, a more advanced could be the current
 * value of 200 days moving average for the price of some CurrencyPair.
 */
public interface Indicator {

    BigDecimal value(RobotExecutionContext ctx);
}
