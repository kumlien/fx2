package hoggaster.robot.web;

import hoggaster.candles.CandleService;
import hoggaster.domain.OrderService;
import hoggaster.oanda.OandaProperties;
import hoggaster.robot.Robot;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.robot.RobotRegistry;
import hoggaster.talib.TALibService;
import hoggaster.user.depot.Depot;
import hoggaster.user.depot.DepotRepo;
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

    private final DepotRepo depotRepo;

    @Autowired
    // TODO Only for testing!! Used to set up a depot.
    private OandaProperties oandaProps;

    @Autowired
    public RobotController(RobotRegistry robotRegistry, RobotDefinitionRepo robotRepo, EventBus priceEventBus, @Qualifier("OandaOrderService") OrderService oandaOrderService, TALibService taLibService, CandleService candleService, DepotRepo depotRepo) {
        this.robotRegistry = robotRegistry;
        this.robotRepo = robotRepo;
        this.priceEventBus = priceEventBus;
        this.oandaOrderService = oandaOrderService;
        this.taLibService = taLibService;
        this.candleService = candleService;
        this.depotRepo = depotRepo;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Robot> getRobots() {
        return robotRegistry.getAllKnownRobots();
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public Robot startRobot(@PathVariable(value = "id") String robotId) {
        LOG.info("Starting robot with id {}", robotId);
        Robot robot = robotRegistry.getById(robotId);
        if (robot == null) {
            RobotDefinition definition = robotRepo.findOne(robotId);
            if (definition == null) {
                throw new IllegalArgumentException("No robotdefinition with id: " + robotId);
            }

            Depot depot = depotRepo.findOne(definition.getDepotId());
            RulesEngine ruleEngine = RulesEngineBuilder.aNewRulesEngine().named("RuleEngine for robot " + definition.name).build();
            robot = new Robot(depot, definition, priceEventBus, oandaOrderService, ruleEngine, taLibService, candleService);
            robotRegistry.add(robot);
        }

        if (robot.isRunning()) {
            LOG.info("Robot with id {} is allready started.", robotId);
        } else {
            robot.start();
            LOG.info("Robot with id {} is now started!", robotId);
        }
        return robot;
    }
}
