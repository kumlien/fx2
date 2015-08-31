package hoggaster.robot;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.domain.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.prices.Price;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.Operator;
import hoggaster.rules.conditions.BuyOrSell;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import hoggaster.user.Depot;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

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
    
    @Mock
    OrderService orderService;
    
    @Mock
    Registration registration;
    
    @Before
    public void before() {
	depot = new Depot(Broker.OANDA, "1");
	depot.sold();
	definition = new RobotDefinition("Frekkin robot!", Instrument.USD_SEK);
	Mockito.when(priceEventBus.on(Mockito.any(), Mockito.any())).thenReturn(registration);
    }
    
    @Test
    public void testSimpleBuyOnPrice() {
	SimpleValueIndicator svi = new SimpleValueIndicator(2.0); //First indicator
	CurrentAskIndicator cai = new CurrentAskIndicator(); //Second indicator
	//Let's compare them in a condition, putting an operator between them
	TwoIndicatorCondition condition = new TwoIndicatorCondition("Buy when ask is > 2", cai, svi, Operator.GREATER_THAN, 2, BuyOrSell.BUY, MarketUpdateType.PRICE);
	definition.addBuyCondition(condition);
	Robot robot = new Robot(depot, definition, maService, priceEventBus, orderService);
	robot.start(); 
	Price price = new Price(Instrument.USD_SEK, 1.99, 2.01, Instant.now(), Broker.OANDA);
	robot.accept(Event.wrap(price));
	Mockito.verify(orderService).sendOrder(Mockito.any(OrderRequest.class));
    }
    
    @Test
    public void noBuyIfInstrumentOwned() {
	SimpleValueIndicator svi = new SimpleValueIndicator(2.0); //First indicator
	CurrentAskIndicator cai = new CurrentAskIndicator(); //Second indicator
	//Let's compare them in a condition, putting an operator between them
	TwoIndicatorCondition condition = new TwoIndicatorCondition("Buy when ask is > 2", cai, svi, Operator.GREATER_THAN, 2, BuyOrSell.BUY, MarketUpdateType.PRICE);
	definition.addBuyCondition(condition);
	depot.bought(Instrument.USD_SEK, new BigDecimal(100.0), new BigDecimal(100.0));
	Robot robot = new Robot(depot, definition, maService, priceEventBus, orderService);
	robot.start(); 
	Price price = new Price(Instrument.USD_SEK, 1.99, 2.01, Instant.now(), Broker.OANDA);
	robot.accept(Event.wrap(price));
	Mockito.verify(orderService).sendOrder(Mockito.any(OrderRequest.class));
    }
}
