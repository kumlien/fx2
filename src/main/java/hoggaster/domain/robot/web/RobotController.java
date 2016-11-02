package hoggaster.domain.robot.web;

import hoggaster.candles.CandleService;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotImpl;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.robot.Robot;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.robot.RobotService;
import hoggaster.domain.trades.TradeService;
import hoggaster.talib.TALibService;
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

    private final RobotService robotService;

    private final TALibService taLibService;

    private final EventBus priceEventBus;

    private final BrokerConnection brokerConnection;

    private final CandleService candleService;

    private final DepotService depotService;

    private final PriceService priceService;

    private final TradeService tradeService;



    @Autowired
    public RobotController(RobotService robotService, EventBus priceEventBus, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, TALibService taLibService, CandleService candleService, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.robotService = robotService;
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
        return robotService.getAllKnownRobots();
    }

    //Well, this was an interesting mapping for starting something... TODO
    @RequestMapping(value = "{id}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public Robot startRobot(@PathVariable(value = "depotId") String depotId, @PathVariable(value = "robotId") String robotId) {
        LOG.info("Starting robot with id {}", robotId);
        Robot robot = robotService.getById(robotId);

        //TODO This doesnt work, need to fetch the depot and then the robot from the depot.
        if (robot == null) {
            RobotDefinition definition = null;
            if (definition == null) {
                throw new IllegalArgumentException("No robot definition with id: " + robotId);
            }

            Depot depot = new DepotImpl(depotId, brokerConnection, depotService, priceService, tradeService);

            robot = new Robot(depot, definition, priceEventBus, taLibService, candleService);
        }

        if (robotService.isRunning(robot.id)) {
            LOG.info("Robot with id {} is already started.", robotId);
        } else {
            robotService.start(robot);
            LOG.info("Robot with id {} is now started!", robotId);
        }
        return robot;
    }
}
