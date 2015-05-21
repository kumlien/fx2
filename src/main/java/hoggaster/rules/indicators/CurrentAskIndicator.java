package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

public class CurrentAskIndicator implements Indicator {

	@Override
	public Double value(RobotExecutionContext ctx) {
		return ctx.price.ask;
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "CurrentAskIndicator";
	}

}
