package hoggaster.domain.robot.web;

import hoggaster.candles.CandleService;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotImpl;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.robot.Robot;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.robot.RobotRegistry;
import hoggaster.domain.trades.TradeService;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.talib.TALibService;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.bus.EventBus;

import java.util.List;

/**
 * Used to start/stop/query running robot instances.
 */
@RestController
@RequestMapping("robots")
public class RobotController {

    private static final Logger LOG = LoggerFactory.getLogger(RobotController.class);

    private final RobotRegistry robotRegistry;

    private final RobotDefinitionRepo robotRepo;

    private final TALibService taLibService;

    private final EventBus priceEventBus;

    private final BrokerConnection brokerConnection;

    private final CandleService candleService;

    private final DepotService depotService;

    private final PriceService priceService;

    private final TradeService tradeService;



    @Autowired
    public RobotController(RobotRegistry robotRegistry, RobotDefinitionRepo robotRepo, EventBus priceEventBus, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, TALibService taLibService, CandleService candleService, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.robotRegistry = robotRegistry;
        this.robotRepo = robotRepo;
        this.priceEventBus = priceEventBus;
        this.brokerConnection = brokerConnection;
        this.taLibService = taLibService;
        this.candleService = candleService;
        this.depotService = depotService;
        this.priceService = priceService;
        this.tradeService = tradeService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Robot> getRobots() {
        return robotRegistry.getAllKnownRobots();
    }

    //Well, this was an interesting mapping for starting something... TODO
    @RequestMapping(value = "{id}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public Robot startRobot(@PathVariable(value = "id") String robotId) {
        LOG.info("Starting robot with id {}", robotId);
        Robot robot = robotRegistry.getById(robotId);
        if (robot == null) {
            RobotDefinition definition = robotRepo.findOne(robotId);
            if (definition == null) {
                throw new IllegalArgumentException("No robot definition with id: " + robotId);
            }

            Depot depot = new DepotImpl(definition.getDepotId(), brokerConnection, depotService, priceService, tradeService);

            RulesEngine ruleEngine = RulesEngineBuilder.aNewRulesEngine().named("RuleEngine for robot " + definition.name).build();
            robot = new Robot(depot, definition, priceEventBus, ruleEngine, taLibService, candleService);
            robotRegistry.add(robot);
        }

        if (robot.isRunning()) {
            LOG.info("Robot with id {} is already started.", robotId);
        } else {
            robot.start();
            LOG.info("Robot with id {} is now started!", robotId);
        }
        return robot;
    }
}
