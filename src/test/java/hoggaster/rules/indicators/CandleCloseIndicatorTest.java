package hoggaster.rules.indicators;

import hoggaster.candles.Candle;
import hoggaster.domain.robot.RobotExecutionContext;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static hoggaster.domain.CurrencyPair.USD_CAD;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by svante2 on 2016-11-07.
 */
@RunWith(MockitoJUnitRunner.class)
public class CandleCloseIndicatorTest {


    @Mock
    RobotExecutionContext ctx;

    CandleCloseIndicator candleCloseIndicator;


    @Test
    public void value() throws Exception {
        BigDecimal closeBid = new BigDecimal("2.0");
        candleCloseIndicator = new CandleCloseIndicator(CandleStickGranularity.END_OF_DAY);
        Candle candle = Candle.Builder.aCandle(USD_CAD, CandleStickGranularity.END_OF_DAY, Instant.now()).withCloseBid(closeBid).build();

        when(ctx.getLatestCandle(CandleStickGranularity.END_OF_DAY)).thenReturn(Optional.of(candle));
        BigDecimal value = candleCloseIndicator.value(ctx);
        assertEquals(closeBid, value);
    }

}