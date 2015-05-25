package hoggaster.robot.web;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerID;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.robot.MovingAverageService;
import hoggaster.robot.Robot;
import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.robot.RobotRegistry;
import hoggaster.user.Depot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.bus.EventBus;

/**
 * Used to start/stop/query running robot instances.
 * 
 */
@RestController
@RequestMapping("robots")
public class RobotController {
	
	private static final Logger LOG = LoggerFactory.getLogger(RobotController.class);

	private final RobotRegistry robotRegistry;
	
	private final RobotDefinitionRepo robotRepo;
	
	private final MovingAverageService maService;
	
	private final EventBus priceEventBus;
	
	private final Broker oanda;
	
	@Autowired //TODO Only for testing!! Used to set up a depot. 
	private OandaProperties oandaProps;
	
	
	@Autowired
	public RobotController(RobotRegistry robotRegistry, RobotDefinitionRepo robotRepo, MovingAverageService maService, EventBus priceEventBus, Broker oandaApi) {
		this.robotRegistry = robotRegistry;
		this.robotRepo = robotRepo;
		this.maService = maService;
		this.priceEventBus = priceEventBus;
		this.oanda = oandaApi;
	}
	
	
	@RequestMapping(method=RequestMethod.GET)
	public List<Robot> getRobots() {
		return robotRegistry.getAllKnownRobots();
	}
	
	@RequestMapping(value="{id}", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	public Robot startRobot(@PathVariable(value="id") String robotId) {
		LOG.info("Starting robot with id {}", robotId);
		Robot robot = robotRegistry.getById(robotId);
		if(robot == null) {
			RobotDefinition definition = robotRepo.findOne(robotId);
			if(definition == null) {
				throw new IllegalArgumentException("No robotdefinition with id: " + robotId);
			}
			
			//TODO For now hardwired to oanda and main account
			Depot depot = new Depot(BrokerID.OANDA, String.valueOf(oandaProps.getMainAccountId()));
			robot = new Robot(depot, definition, maService, priceEventBus, oanda);
			robotRegistry.add(robot);
		}
		
		if(robot.isRunning()) {
			LOG.info("Robot with id {} is allready started.", robotId);
		} else {
			robot.start();
			LOG.info("Robot with id {} is now started!", robotId);
		}
		return robot;
	}
}
