package it;

import hoggaster.Application;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.rules.Condition;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.conditions.Side;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.*;
import hoggaster.user.Depot;
import hoggaster.user.DepotRepo;
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

import java.math.BigDecimal;

import static hoggaster.rules.Comparator.GREATER_OR_EQUAL_THAN;
import static hoggaster.rules.Comparator.LESS_OR_EQUAL_THAN;
import static hoggaster.rules.MarketUpdateType.ONE_DAY_CANDLE;
import static hoggaster.rules.conditions.Side.BUY;
import static hoggaster.rules.indicators.CandleStickField.CLOSE_BID;
import static hoggaster.rules.indicators.CandleStickGranularity.END_OF_DAY;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DepotCRUDTest {

    private static final Logger LOG = LoggerFactory.getLogger(DepotCRUDTest.class);

    @Value("${local.server.port}")
    int port;

    @Autowired
    DepotRepo depotRepo;


    /**
     */
    @Test
    public void testCreatePellesDepot() throws InterruptedException {
        Depot depot = new Depot("Pelles depot", Broker.OANDA, "Primary", "9678914",new BigDecimal(0.05));
        depotRepo.save(depot);
    }


}
