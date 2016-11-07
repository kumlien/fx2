package hoggaster.rules.indicators;

import hoggaster.candles.Candle;
import hoggaster.domain.robot.RobotExecutionContext;
import hoggaster.rules.indicators.candles.CandleStickGranularity;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by svante2 on 2016-11-07.
 */
public class CandleCloseIndicator implements Indicator {

    public final CandleStickGranularity candleStickGranularity;

    public CandleCloseIndicator(CandleStickGranularity candleStickGranularity) {
        this.candleStickGranularity = candleStickGranularity;
    }

    @Override
    public BigDecimal value(RobotExecutionContext ctx) {
        Optional<Candle> latestCandle = ctx.getLatestCandle(candleStickGranularity);
        if(latestCandle.isPresent()) {
            return latestCandle.get().closeBid;
        }
        throw new NoIndicatorAvailableException(this, "No candle found for currency pair " + ctx.getCurrencyPair() + " and granularity " + candleStickGranularity);
    }
}
