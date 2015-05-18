package hoggaster.rules;

import hoggaster.robot.RobotExecutionContext;


/**
 * This interface represents some kind of indicator, a very basic indicator would
 * be the current price of an Instrument, a more advanced could be the current
 * value of 200 days moving average for the price of some Instrument.
 */
public interface Indicator {
	
	Double value(RobotExecutionContext ctx);
}
