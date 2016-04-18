package hoggaster.rules.indicators;

import com.tictactec.ta.lib.RetCode;
import hoggaster.domain.robot.RobotExecutionContext;
import hoggaster.talib.TAResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static hoggaster.rules.indicators.CandleStickField.CLOSE_BID;
import static hoggaster.rules.indicators.CandleStickGranularity.MINUTE;

@RunWith(MockitoJUnitRunner.class)
public class RSIIndicatorTest {

    @Mock
    RobotExecutionContext ctx;

    @Test
    public void testThatGetRSIIsCalled() {
        double values[] = new double[]{46.1250, 47.1250, 46.4375, 46.9375, 44.9375, 44.2500, 44.6250, 45.7500, 47.8125, 47.5625, 47.0000, 44.5625, 46.3125, 47.6875, 46.6875, 45.6875, 43.0625, 43.5625, 44.8750, 43.6875};
        TAResult result = new TAResult(RetCode.Success, values, 2, 20);
        RSIIndicator indicator = new RSIIndicator(2, 10, 0, MINUTE, CLOSE_BID);

        Mockito.when(ctx.getRSI(Mockito.eq(MINUTE), Mockito.eq(2), Mockito.eq(10), Mockito.eq(CLOSE_BID))).thenReturn(result);
        indicator.value(ctx);
        Mockito.verify(ctx).getRSI(Mockito.eq(MINUTE), Mockito.eq(2), Mockito.eq(10), Mockito.eq(CLOSE_BID));
    }
}
