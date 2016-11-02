package hoggaster.domain.robot;

import com.google.common.base.Preconditions;
import hoggaster.candles.CandleService;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotImpl;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.TradeService;
import hoggaster.talib.TALibService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.bus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for running robots. Right now just a wrapper for a map of robots...
 */
@Service
public class RobotService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RobotService.class);

    private final TALibService taLibService;

    private final EventBus priceEventBus;

    private final BrokerConnection brokerConnection;

    private final CandleService candleService;

    private final DepotService depotService;

    private final PriceService priceService;

    private final TradeService tradeService;

    //stoopid registry for now
    private final Map<String, Robot> robots = new ConcurrentHashMap<String, Robot>();

    @Autowired
    public RobotService(TALibService taLibService, EventBus priceEventBus, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, CandleService candleService, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.taLibService = taLibService;
        this.priceEventBus = priceEventBus;
        this.brokerConnection = brokerConnection;
        this.candleService = candleService;
        this.depotService = depotService;
        this.priceService = priceService;
        this.tradeService = tradeService;
    }

    public List<Robot> getAllKnownRobots() {
        return new ArrayList<>(robots.values());
    }

    public Robot getById(String robotId) {
        return robots.get(robotId);
    }

    public void start(Robot robot) {
        LOG.info("Starting {}", robot);
        synchronized (robots) {
            Preconditions.checkArgument(!robots.containsKey(robot.id), "There is already a robot with id " + robot.id + " in the registry");
            robot.start();
            this.robots.put(robot.id, robot);
        }
    }

    public void start(RobotDefinition robotDefinition, String depotId) {
        Depot depot = new DepotImpl(depotId, brokerConnection, depotService, priceService, tradeService);
        Robot robot = new Robot(depot, robotDefinition, priceEventBus, taLibService, candleService);
        start(robot);
    }

    public void stop(Robot robot) {
        synchronized (robots) {
            Preconditions.checkArgument(robots.containsKey(robot.id), "There is no running robot with id " + robot.id + " in the registry");
            robot.stop();
            this.robots.remove(robot.id);
        }
    }


    public boolean isRunning(String id) {
        return robots.containsKey(id);
    }


    public void stop(String robotId) {
        Preconditions.checkArgument(StringUtils.hasText(robotId), "The specified robotId is null or empty");
        synchronized (robots) {
            Preconditions.checkArgument(robots.containsKey(robotId), "There is no running robot with id " + robotId + " in the registry");
            Robot robot = robots.get(robotId);
            robot.stop();
            this.robots.remove(robotId);
            LOG.info("Stopped robot {}", robot);
        }
    }
}
