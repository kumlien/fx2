package it;

import hoggaster.MongoConfig;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.trades.TradeAction;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.conditions.Condition;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static hoggaster.rules.Comparator.GREATER_OR_EQUAL_THAN;
import static hoggaster.rules.Comparator.LESS_OR_EQUAL_THAN;
import static hoggaster.rules.MarketUpdateType.ONE_DAY_CANDLE;
import static hoggaster.rules.indicators.CandleStickField.CLOSE_BID;
import static hoggaster.rules.indicators.CandleStickGranularity.END_OF_DAY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MongoConfig.class)
public class RobotDefintionCRUDTest {

    private static final Logger LOG = LoggerFactory.getLogger(RobotDefintionCRUDTest.class);

    /**
     * Create a robot with two sendOrder conditions:
     * 1) close bid of one-day candle is >= MA200 for one-day candle close bid
     * 2) (two day rsi(today) + two day rsi(yesterday)) <=10
     *
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void testCreatePellesRobot() throws InterruptedException {
        RobotDefinition robotDefinition = new RobotDefinition("PellesRobot", CurrencyPair.EUR_USD, OrderSide.buy);

        RSIIndicator rsi1 = new RSIIndicator(2, 100, 0, END_OF_DAY, CLOSE_BID); //rsi with 2 periods, minimum 100 data points, look at first value, type of candle is DAY and field to use is close_bid
        RSIIndicator rsi2 = new RSIIndicator(2, 100, 1, END_OF_DAY, CLOSE_BID);
        CompoundIndicator compoundRSIIndicator = new CompoundIndicator(rsi1, rsi2, CompoundIndicator.Operator.ADD);
        SimpleValueIndicator simpleValueIndicator = new SimpleValueIndicator(new BigDecimal("10.0"));
        Condition firstBuyCondition = new TwoIndicatorCondition("rsi1 + rsi2 should be <= 10", compoundRSIIndicator, simpleValueIndicator, LESS_OR_EQUAL_THAN, 0, TradeAction.OPEN, ONE_DAY_CANDLE);
        robotDefinition.addEnterTradeCondition(firstBuyCondition);

        CandleIndicator oneDayCloseCandleIndicator = new CandleIndicator(CandleStickGranularity.END_OF_DAY, CLOSE_BID);
        SMAIndicator smaForOneDayClose200Days = new SMAIndicator(END_OF_DAY,500, CLOSE_BID, 200);
        TwoIndicatorCondition secondBuyCondition = new TwoIndicatorCondition("Close bid for the one day candle should be >= SMA200 for one day candle close bid",oneDayCloseCandleIndicator,smaForOneDayClose200Days, GREATER_OR_EQUAL_THAN,0, TradeAction.OPEN, ONE_DAY_CANDLE);
        robotDefinition.addEnterTradeCondition(secondBuyCondition);

        //robotDefinition = robotRepo.save(robotDefinition);

        //robotRepo.delete(robotDefinition.getId());
    }

    @Test
    public void testCRUDRobotDefinition() throws InterruptedException {
        RobotDefinition rd = new RobotDefinition("myRobotDefinition", CurrencyPair.AUD_USD, OrderSide.buy);
        TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(new BigDecimal(150.0)), GREATER_OR_EQUAL_THAN, 1, TradeAction.OPEN, MarketUpdateType.ONE_MINUTE_CANDLE);
        rd.addEnterTradeCondition(buyCondition);
        //rd = robotRepo.save(rd);
        Assert.assertNotNull(rd.getId());
        LOG.info("RobotDefinition saved to db with id {}", rd.getId());

        TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(new BigDecimal(140.0)), LESS_OR_EQUAL_THAN, 1, TradeAction.OPEN);
        TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(new BigDecimal(160.0)), GREATER_OR_EQUAL_THAN, 2, TradeAction.OPEN);
        rd.addExitTradeCondition(stopLoss);
        rd.addExitTradeCondition(takeProfit);
        //rd = robotRepo.save(rd);

        //robotRepo.delete(rd.getId());
    }


    @Test
    @Ignore
    public void testCRUDRobotDefinition2() throws InterruptedException {
        RobotDefinition rd = new RobotDefinition("Robot2", CurrencyPair.EUR_USD, OrderSide.buy);
        TwoIndicatorCondition buyCondition = new TwoIndicatorCondition("Buy when ask is >= 150", new CurrentAskIndicator(), new SimpleValueIndicator(new BigDecimal(150.0)), GREATER_OR_EQUAL_THAN, 1, TradeAction.OPEN);
        rd.addEnterTradeCondition(buyCondition);
        //rd = robotRepo.save(rd);
        Assert.assertNotNull(rd.getId());
        LOG.info("RobotDefinition saved to db with id {}", rd.getId());

        TwoIndicatorCondition stopLoss = new TwoIndicatorCondition("Sell when ask is <= 140", new CurrentAskIndicator(), new SimpleValueIndicator(new BigDecimal(140.0)), LESS_OR_EQUAL_THAN, 1, TradeAction.OPEN);
        TwoIndicatorCondition takeProfit = new TwoIndicatorCondition("Sell when bid is >= 160", new CurrentBidIndicator(), new SimpleValueIndicator(new BigDecimal(160.0)), GREATER_OR_EQUAL_THAN, 2, TradeAction.OPEN);
        rd.addExitTradeCondition(stopLoss);
        rd.addExitTradeCondition(takeProfit);
        //rd = robotRepo.save(rd);

        //robotRepo.delete(rd.getId());
    }
}
