package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.robot.RobotExecutionContext;


/**
 * Indicator which gets it's value from a {@link Candle}.
 *
 * @author svante2
 */
public class CandleIndicator implements Indicator {

    public final CandleStickGranularity granularity;

    public final CandleStickField field;

    /**
     * ctor
     *
     * @param granularity The {@link CandleStickGranularity} we react to.
     * @param field       The field on the candle stick which we use.
     */
    public CandleIndicator(CandleStickGranularity granularity, CandleStickField field) {
        this.granularity = granularity;
        this.field = field;
    }

    @Override
    public Double value(RobotExecutionContext ctx) {
        Preconditions.checkArgument(ctx != null);
        Preconditions.checkArgument(ctx.marketUpdate != null);
        Preconditions.checkArgument(ctx.marketUpdate.getType().isCandle());
        return ((Candle) ctx.marketUpdate).getValue(field);
    }
}
