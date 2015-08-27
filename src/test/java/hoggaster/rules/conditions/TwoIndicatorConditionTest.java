package hoggaster.rules.conditions;

import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.prices.Price;
import hoggaster.robot.MovingAverageServiceImpl;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.EventType;
import hoggaster.rules.Indicator;
import hoggaster.rules.Operator;
import hoggaster.rules.indicators.CurrentAskIndicator;
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
		Price price = new Price(instrument, 2.0, 2.1, Instant.now(), Broker.OANDA);
		Depot depot = new Depot(Broker.OANDA, "13123");
		MovingAverageServiceImpl maService = Mockito.mock(MovingAverageServiceImpl.class);
		
		Indicator firstIndicator = new CurrentAskIndicator();
		Indicator secondIndicator = new SimpleValueIndicator(2.0);
		TwoIndicatorCondition tic = new TwoIndicatorCondition("Test current ask greater than 2.0", firstIndicator, secondIndicator, Operator.GREATER_THAN, 1, BuyOrSell.BUY, EventType.ONE_MINUTE_CANDLE);
		RobotExecutionContext ctx = new RobotExecutionContext(price, depot, instrument, maService, EventType.ONE_MINUTE_CANDLE);
		tic.setContext(ctx);
		
		AnnotatedRulesEngine annotatedRulesEngine = new AnnotatedRulesEngine();		
		annotatedRulesEngine.registerRule(tic);
		annotatedRulesEngine.fireRules();
		
		Assert.assertTrue(ctx.getPositiveBuyConditions().size() > 0);
	}
}
