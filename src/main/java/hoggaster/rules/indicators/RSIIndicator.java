package hoggaster.rules.indicators;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.talib.TAResult;

/**
 * {@link Indicator} used to get an rsi value from a set of candle stick values.
 */
public class RSIIndicator implements Indicator {
    
    //The number of periods to use in the calculation
    private final int periods;
    
    //The minimum number of data points needed to perform the calculation
    private final int dataPointsNeeded;
    
    //Which rsi value are we interested in? 0 indicates the latest rsi value. 1 indicates the value before the last one (und so weiter...)
    private final int indexOfInterest;

    //What kind of candle we use
    private final CandleStickGranularity granularity;
    
    private final CandleStickField field;

    public RSIIndicator(int periods, int dataPointsNeeded, int indexOfInterest, CandleStickGranularity granularity, CandleStickField field) {
	this.periods = periods;
	this.dataPointsNeeded = dataPointsNeeded;
	this.indexOfInterest = indexOfInterest;
	this.granularity = granularity;
	this.field = field;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
	TAResult result = ctx.getRSI(granularity, periods, dataPointsNeeded, field);
	return result.values.get(indexOfInterest);
    }
}
