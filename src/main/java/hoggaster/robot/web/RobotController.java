package hoggaster.robot.web;

import hoggaster.candles.CandleService;
import hoggaster.depot.Depot;
import hoggaster.depot.DepotImpl;
import hoggaster.depot.DepotService;
import hoggaster.domain.orders.OrderService;
import hoggaster.prices.PriceService;
import hoggaster.robot.Robot;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.robot.RobotRegistry;
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

    private final OrderService oandaOrderService;

    private final CandleService candleService;

    private final DepotService depotService;

    private final PriceService priceService;



    @Autowired
    public RobotController(RobotRegistry robotRegistry, RobotDefinitionRepo robotRepo, EventBus priceEventBus, @Qualifier("OandaOrderService") OrderService oandaOrderService, TALibService taLibService, CandleService candleService, DepotService depotService, PriceService priceService) {
        this.robotRegistry = robotRegistry;
        this.robotRepo = robotRepo;
        this.priceEventBus = priceEventBus;
        this.oandaOrderService = oandaOrderService;
        this.taLibService = taLibService;
        this.candleService = candleService;
        this.depotService = depotService;
        this.priceService = priceService;
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

            Depot depot = new DepotImpl(definition.getDepotId(), oandaOrderService, depotService, priceService);

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
