package hoggaster.rules.conditions;

import hoggaster.candles.BidAskCandle;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.robot.MovingAverageServiceImpl;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.Operator;
import hoggaster.rules.indicators.BidAskCandleIndicator;
import hoggaster.rules.indicators.CandleStickField;
import hoggaster.rules.indicators.CandleStickGranularity;
import hoggaster.rules.indicators.Indicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import hoggaster.user.Depot;

import java.time.Instant;

import org.easyrules.core.AnnotatedRulesEngine;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TwoIndicatorConditionTest {

	@Test
	public void testTriggerBuyOnOneMinuteCandle() {
		Instrument instrument = Instrument.AUD_USD;
		BidAskCandle candle = new BidAskCandle(instrument, Broker.OANDA, CandleStickGranularity.MINUTE, Instant.now(), 2.0, 2.1, 2.4, 2.45, 1.9, 2.0, 2.3, 2.35, 1000, true);
		Depot depot = new Depot(Broker.OANDA, "13123");
		MovingAverageServiceImpl maService = Mockito.mock(MovingAverageServiceImpl.class);
		
		Indicator firstIndicator = new BidAskCandleIndicator(CandleStickGranularity.MINUTE, CandleStickField.CLOSE_BID);
		Indicator secondIndicator = new SimpleValueIndicator(2.0);
		TwoIndicatorCondition tic = new TwoIndicatorCondition("Test current ask greater than 2.0", firstIndicator, secondIndicator, Operator.GREATER_THAN, 1, BuyOrSell.BUY, MarketUpdateType.ONE_MINUTE_CANDLE);
		RobotExecutionContext ctx = new RobotExecutionContext(candle, depot, instrument, maService);
		tic.setContext(ctx);
		
		AnnotatedRulesEngine annotatedRulesEngine = new AnnotatedRulesEngine();		
		annotatedRulesEngine.registerRule(tic);
		annotatedRulesEngine.fireRules();
		
		Assert.assertTrue(ctx.getPositiveBuyConditions().size() > 0);
	}
}
