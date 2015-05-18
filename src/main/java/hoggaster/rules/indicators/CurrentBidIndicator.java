package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

public class CurrentBidIndicator implements Indicator {

	@Override
	public Double value(RobotExecutionContext ctx) {
		return ctx.price.bid;
	}

	@Override
	public String toString() {
		return "CurrentBidIndicator";
	}

}
