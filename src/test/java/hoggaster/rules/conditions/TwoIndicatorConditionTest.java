package hoggaster.rules.conditions;

import hoggaster.domain.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.prices.Price;
import hoggaster.robot.MovingAverageService;
import hoggaster.robot.RobotExecutionContext;
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
	public void basicTest() {
		Instrument instrument = Instrument.AUD_USD;
		Price price = new Price(instrument, 2.0, 2.1, Instant.now(), BrokerID.OANDA);
		Depot depot = new Depot(BrokerID.OANDA, "13123");
		MovingAverageService maService = Mockito.mock(MovingAverageService.class);
		
		Indicator firstIndicator = new CurrentAskIndicator();
		Indicator secondIndicator = new SimpleValueIndicator(2.0);
		TwoIndicatorCondition tic = new TwoIndicatorCondition("Test current ask greater than 2.0", firstIndicator, secondIndicator, Operator.GREATER_THAN, 1, ConditionType.BUY);
		RobotExecutionContext ctx = new RobotExecutionContext(price, depot, instrument, maService);
		tic.setContext(ctx);
		
		AnnotatedRulesEngine annotatedRulesEngine = new AnnotatedRulesEngine();		
		annotatedRulesEngine.registerRule(tic);
		annotatedRulesEngine.fireRules();
		
		Assert.assertTrue(ctx.getPositiveBuyConditions().size() > 0);
	}
}
