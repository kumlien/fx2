package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.robot.RobotExecutionContext;

/**
 * {@link Indicator} used to get a simple moving average value for a
 * {@link Candle} with the specified {@link CandleStickGranularity}.
 * <p>
 * Right now hard coded to use the 'youngest' sma value.
 */
public class SMAIndicator implements Indicator {

    public final CandleStickGranularity granularity;

    public final Integer minimumNoOfDataPoints;

    private final CandleStickField field;

    private final int periods;

    /**
     * Brand new!
     *
     * @param granularity        The granularity of the candle
     * @param minimumNoOfDataPoints The minimum number of data points needed
     * @param field              Which field in the {@link Candle} we should look at
     * @param periods            How many periods the sma should be calculated with.
     */
    public SMAIndicator(CandleStickGranularity granularity, Integer minimumNoOfDataPoints, CandleStickField field, int periods) {
        Preconditions.checkArgument(minimumNoOfDataPoints > periods, "The minimum number of data points must by higher than the specified number of periods (" + minimumNoOfDataPoints + " vs " + periods);
        this.granularity = granularity;
        this.minimumNoOfDataPoints = minimumNoOfDataPoints;
        this.field = field;
        this.periods = periods;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
        return ctx.getSMA(granularity, minimumNoOfDataPoints, field, periods).values.get(0);
    }
}
