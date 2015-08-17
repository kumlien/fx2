package hoggaster.robot;

import static hoggaster.rules.EventType.ONE_MINUTE_CANDLE;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.rules.Operator;
import hoggaster.rules.conditions.ConditionType;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import hoggaster.user.Depot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import reactor.bus.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class BasicRobotTest {

    Depot depot;
    
    RobotDefinition definition;
    
    @Mock
    MovingAverageService maService;
    
    @Mock
    EventBus priceEventBus;
    
    @Mock
    BrokerConnection broker;
    
    @Before
    public void before() {
	depot = new Depot(Broker.OANDA, "1");
	definition = new RobotDefinition("my def", Instrument.USD_SEK);
	
	
	
    }
    
    @Test
    public void testSimpleBuy() {
	SimpleValueIndicator svi = new SimpleValueIndicator(2.0);
	CurrentAskIndicator cai = new CurrentAskIndicator();
	TwoIndicatorCondition condition = new TwoIndicatorCondition("Buy when ask is > 2", cai, svi, Operator.GREATER_THAN, 1, ConditionType.BUY, ONE_MINUTE_CANDLE);
	definition.addBuyCondition(condition);
	Robot robot = new Robot(depot, definition, maService, priceEventBus, broker);
	

	robot.accept(null);
    }
}
