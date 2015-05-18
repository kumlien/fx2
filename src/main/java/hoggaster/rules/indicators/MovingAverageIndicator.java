package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

public class MovingAverageIndicator implements Indicator {
	
	public final CandleStickGranularity granularity;

	public MovingAverageIndicator(CandleStickGranularity granularity) {
		this.granularity = granularity;
	}

	@Override
	public Double value(RobotExecutionContext ctx) {
		return ctx.getMovingAverage(granularity);
	}
}
