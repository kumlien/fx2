package hoggaster.rules.conditions;

import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.indicators.*;
import hoggaster.talib.TALibService;
import hoggaster.user.Depot;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;

@RunWith(MockitoJUnitRunner.class)
public class TwoIndicatorConditionTest {

    @Mock
    CandleService candleService;

    @Mock
    TALibService taLibService;

    @Test
    public void testTriggerBuyOnOneMinuteCandle() {
        Instrument instrument = Instrument.AUD_USD;
        Candle candle = new Candle(instrument, Broker.OANDA, CandleStickGranularity.MINUTE, Instant.now(), 2.0, 2.1, 2.4, 2.45, 1.9, 2.0, 2.3, 2.35, 1000, true);
        Depot depot = new Depot("A test depot", Broker.OANDA, "brokerDepotName", "13123", new BigDecimal(0.05));

        Indicator firstIndicator = new CandleIndicator(CandleStickGranularity.MINUTE, CandleStickField.CLOSE_BID);
        Indicator secondIndicator = new SimpleValueIndicator(2.0);
        TwoIndicatorCondition tic = new TwoIndicatorCondition("Test current ask greater than 2.0", firstIndicator, secondIndicator, Comparator.GREATER_THAN, 1, Side.BUY, MarketUpdateType.ONE_MINUTE_CANDLE);
        RobotExecutionContext ctx = new RobotExecutionContext(candle, depot, instrument, taLibService, candleService);
        tic.setContext(ctx);

        RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine().build();
        rulesEngine.registerRule(tic);
        rulesEngine.fireRules();

        Assert.assertTrue(ctx.getPositiveBuyConditions().size() > 0);
    }
}
