package hoggaster.rules.indicators;

import hoggaster.candles.Candle;
import hoggaster.robot.RobotExecutionContext;

/**
 * {@link Indicator} used to get a moving average value for a
 * {@link Candle}
 * 
 */
public class MovingAverageIndicator implements Indicator {

    public final CandleStickGranularity granularity;

    public final Integer numberOfDataPoints;

    private final CandleStickField field;
    
    private final int periods;

    public MovingAverageIndicator(CandleStickGranularity granularity, Integer numberOfDataPoints, CandleStickField field, int periods) {
	this.granularity = granularity;
	this.numberOfDataPoints = numberOfDataPoints;
	this.field = field;
	this.periods = periods;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
	return ctx.getSimpleMovingAverage(granularity, numberOfDataPoints, field, periods);
    }
}
