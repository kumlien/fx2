package hoggaster.rules.indicators;

import hoggaster.candles.BidAskCandle;
import hoggaster.robot.RobotExecutionContext;

import com.google.common.base.Preconditions;


/**
 * Indicator which gets it's value from a {@link BidAskCandle}.
 * 
 * @author svante2
 *
 */
public class BidAskCandleIndicator implements Indicator {
    
    public final CandleStickGranularity granularity;
    
    public final CandleStickField field;
    
    /**
     * ctor
     * 
     * @param granularity The {@link CandleStickGranularity} we react to.
     * @param field The field on the candle stick which we use.
     */
    public BidAskCandleIndicator(CandleStickGranularity granularity, CandleStickField field) {
	this.granularity = granularity;
	this.field = field;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
	Preconditions.checkArgument(ctx != null);
	Preconditions.checkArgument(ctx.marketUpdate != null);
	Preconditions.checkArgument(ctx.marketUpdate.getType() == granularity.type);
	return ((BidAskCandle)ctx.marketUpdate).getValue(field);
    }
}
