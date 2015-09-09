package it;

import static hoggaster.rules.Comparator.LESS_OR_EQUAL_THAN;
import static hoggaster.rules.MarketUpdateType.ONE_DAY_CANDLE;
import static hoggaster.rules.conditions.BuyOrSell.BUY;
import static hoggaster.rules.indicators.CandleStickGranularity.END_OF_DAY;
import hoggaster.Application;
import hoggaster.domain.Instrument;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.conditions.BuyOrSell;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CandleStickField;
import hoggaster.rules.indicators.CompoundIndicator;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.CurrentBidIndicator;
import hoggaster.rules.indicators.RSIIndicator;
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
    
    
    /**
     * Create a robot with two buy conditions:
     * 	1) close bid of one-day candle is >= MA200 for one-day candle close bid
     *  2)  
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCreatePellesRobot() throws InterruptedException {
	RobotDefinition robotDefinition = new RobotDefinition("PellesRobot", Instrument.EUR_USD);
	RSIIndicator rsi1 = new RSIIndicator(2, 100, 0, END_OF_DAY, CandleStickField.CLOSE_BID); //rsi with 2 periods, minimum 100 data points, look at last value and type of candle is DAY
	RSIIndicator rsi2 = new RSIIndicator(2, 100, 1, END_OF_DAY, CandleStickField.CLOSE_BID);
	CompoundIndicator compoundRSIIndicator = new CompoundIndicator(rsi1, rsi2, CompoundIndicator.Operator.ADD);
	SimpleValueIndicator simpleValueIndicator = new SimpleValueIndicator(10.0);
	TwoIndicatorCondition firstBuyCondition = new TwoIndicatorCondition("rsi1 + rsi2 should be <= 10", compoundRSIIndicator, simpleValueIndicator, LESS_OR_EQUAL_THAN, 0, BUY, ONE_DAY_CANDLE);
	
	robotDefinition.addBuyCondition(firstBuyCondition);
	
	robotDefinition = robotRepo.save(robotDefinition);

	robotRepo.delete(robotDefinition.getId());
    }

    @Test
    public void testCRUDRobotDefinition() throws InterruptedException {
	RobotDefinition rd = new RobotDefinition("myRobotDefinition", Instrument.AUD_CAD);
	TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(150.0), Comparator.GREATER_OR_EQUAL_THAN, 1, BuyOrSell.BUY, MarketUpdateType.ONE_MINUTE_CANDLE);
	rd.addBuyCondition(buyCondition);
	rd = robotRepo.save(rd);
	Assert.assertNotNull(rd.getId());
	LOG.info("RobotDefinition saved to db with id {}", rd.getId());

	TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(140.0), Comparator.LESS_OR_EQUAL_THAN, 1, BuyOrSell.BUY);
	TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(160.0), Comparator.GREATER_OR_EQUAL_THAN, 2, BuyOrSell.BUY);
	rd.addSellCondition(stopLoss);
	rd.addSellCondition(takeProfit);
	rd = robotRepo.save(rd);

	robotRepo.delete(rd.getId());
    }
    
    
    @Test
    public void testCRUDRobotDefinition2() throws InterruptedException {
	RobotDefinition rd = new RobotDefinition("Robot2", Instrument.EUR_USD);
	TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(150.0), Comparator.GREATER_OR_EQUAL_THAN, 1, BuyOrSell.BUY);
	rd.addBuyCondition(buyCondition);
	rd = robotRepo.save(rd);
	Assert.assertNotNull(rd.getId());
	LOG.info("RobotDefinition saved to db with id {}", rd.getId());

	TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(140.0), Comparator.LESS_OR_EQUAL_THAN, 1, BuyOrSell.BUY);
	TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(160.0), Comparator.GREATER_OR_EQUAL_THAN, 2, BuyOrSell.BUY);
	rd.addSellCondition(stopLoss);
	rd.addSellCondition(takeProfit);
	rd = robotRepo.save(rd);

	robotRepo.delete(rd.getId());
    }
}
