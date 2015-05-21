package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

public class MovingAverageIndicator implements Indicator {
	
	public final CandleStickGranularity granularity;
	
	public final Integer numberOfDataPoints;

	public MovingAverageIndicator(CandleStickGranularity granularity, Integer numberOfDataPoints) {
		this.granularity = granularity;
		this.numberOfDataPoints = numberOfDataPoints;
	}

	@Override
	public Double value(RobotExecutionContext ctx) {
		return ctx.getMovingAverage(granularity, numberOfDataPoints);
	}
}
