package hoggaster.rules.indicators;

import hoggaster.candles.Candle;
import hoggaster.robot.RobotExecutionContext;

/**
 * {@link Indicator} used to get a simple moving average value for a
 * {@link Candle} with the specified {@link CandleStickGranularity}.
 * 
 * Right now hard coded to use the 'youngest' sma value.
 * 
 */
public class SMAIndicator implements Indicator {

    public final CandleStickGranularity granularity;

    public final Integer numberOfDataPoints;

    private final CandleStickField field;
    
    private final int periods;

    /**
     * Brand new!
     * 
     * @param granularity The granularity of the candle
     * @param numberOfDataPoints The minimum number of data points needed
     * @param field Which field in the {@link Candle} we should look at
     * @param periods How many periods the sma should be calculated with.
     */
    public SMAIndicator(CandleStickGranularity granularity, Integer numberOfDataPoints, CandleStickField field, int periods) {
	this.granularity = granularity;
	this.numberOfDataPoints = numberOfDataPoints;
	this.field = field;
	this.periods = periods;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
	return ctx.getSMA(granularity, numberOfDataPoints, field, periods).values.get(0);
    }
}
