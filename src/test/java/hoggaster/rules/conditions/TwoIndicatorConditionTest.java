package hoggaster.rules.conditions;

import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.indicators.*;
import hoggaster.talib.TALibService;
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
        CurrencyPair currencyPair = CurrencyPair.AUD_USD;
        Candle candle = new Candle(currencyPair, Broker.OANDA, CandleStickGranularity.MINUTE, Instant.now(), new BigDecimal("2.0"), new BigDecimal("2.1"), new BigDecimal("2.4"), new BigDecimal("2.45"), new BigDecimal("1.9"), new BigDecimal("2.0"), new BigDecimal("2.3"), new BigDecimal("2.35"), 1000, true);
        //DbDepot dbDepot = new DbDepot("USER_ID", "A test dbDepot", Broker.OANDA, "brokerDepotName", "13123", new BigDecimal(0.05), "USD");

        Indicator firstIndicator = new CandleIndicator(CandleStickGranularity.MINUTE, CandleStickField.CLOSE_BID);
        Indicator secondIndicator = new SimpleValueIndicator(new BigDecimal("2.0"));
        TwoIndicatorCondition tic = new TwoIndicatorCondition("Test current ask greater than 2.0", firstIndicator, secondIndicator, Comparator.GREATER_THAN, 1, Side.BUY, MarketUpdateType.ONE_MINUTE_CANDLE);
        RobotExecutionContext ctx = new RobotExecutionContext(candle, currencyPair, taLibService, candleService);
        tic.setContext(ctx);

        RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine().build();
        rulesEngine.registerRule(tic);
        rulesEngine.fireRules();

        Assert.assertTrue(ctx.getPositiveBuyConditions().size() > 0);
    }
}
