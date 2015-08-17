package it;

import hoggaster.Application;
import hoggaster.domain.Instrument;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.rules.EventType;
import hoggaster.rules.Operator;
import hoggaster.rules.conditions.ConditionType;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.CurrentBidIndicator;
import hoggaster.rules.indicators.SimpleValueIndicator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class RobotDefintionCRUDTest {

    private static final Logger LOG = LoggerFactory.getLogger(RobotDefintionCRUDTest.class);

    @Value("${local.server.port}")
    int port;

    @Autowired
    RobotDefinitionRepo robotRepo;

    @Test
    public void testCRUDRobotDefinition() throws InterruptedException {
	RobotDefinition rd = new RobotDefinition("myRobotDefinition", Instrument.AUD_CAD);
	TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(150.0), Operator.GREATER_OR_EQUAL_THAN, 1, ConditionType.BUY, EventType.ONE_MINUTE_CANDLE);
	rd.addBuyCondition(buyCondition);
	rd = robotRepo.save(rd);
	Assert.assertNotNull(rd.getId());
	LOG.info("RobotDefinition saved to db with id {}", rd.getId());

	TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(140.0), Operator.LESS_OR_EQUAL_THAN, 1, ConditionType.BUY);
	TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(160.0), Operator.GREATER_OR_EQUAL_THAN, 2, ConditionType.BUY);
	rd.addSellCondition(stopLoss);
	rd.addSellCondition(takeProfit);
	rd = robotRepo.save(rd);

	robotRepo.delete(rd.getId());
    }
    
    
    @Test
    public void testCRUDRobotDefinition2() throws InterruptedException {
	RobotDefinition rd = new RobotDefinition("Robot2", Instrument.EUR_USD);
	TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(150.0), Operator.GREATER_OR_EQUAL_THAN, 1, ConditionType.BUY);
	rd.addBuyCondition(buyCondition);
	rd = robotRepo.save(rd);
	Assert.assertNotNull(rd.getId());
	LOG.info("RobotDefinition saved to db with id {}", rd.getId());

	TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(140.0), Operator.LESS_OR_EQUAL_THAN, 1, ConditionType.BUY);
	TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(160.0), Operator.GREATER_OR_EQUAL_THAN, 2, ConditionType.BUY);
	rd.addSellCondition(stopLoss);
	rd.addSellCondition(takeProfit);
	rd = robotRepo.save(rd);

	robotRepo.delete(rd.getId());
    }
}
