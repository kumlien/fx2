package hoggaster.domain.robot;

import hoggaster.candles.CandleService;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.prices.Price;
import hoggaster.domain.trades.TradeAction;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import hoggaster.talib.TALibService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.math.BigDecimal;
import java.time.Instant;

import static hoggaster.domain.CurrencyPair.USD_SEK;

@RunWith(MockitoJUnitRunner.class)
public class BasicRobotTest {

    @Mock
    Depot depot;

    RobotDefinition definition;

    @Mock
    CandleService candleService;

    @Mock
    EventBus priceEventBus;

    @Mock
    BrokerConnection broker;

    @Mock
    DepotService depotService;

    @Mock
    TALibService taLibService;

    @Mock
    Registration registration;



    @Before
    public void before() {
        definition = new RobotDefinition("Frekkin robot!", USD_SEK);
        Mockito.when(priceEventBus.on(Mockito.any(), Mockito.any())).thenReturn(registration);
    }

    @Test
    public void testSimpleBuyOnPrice() {
        SimpleValueIndicator svi = new SimpleValueIndicator(new BigDecimal("2")); //First indicator
        CurrentAskIndicator cai = new CurrentAskIndicator(); //Second indicator
        //Let's compare them in a condition, putting an operator between them
        TwoIndicatorCondition condition = new TwoIndicatorCondition("Buy when ask is > 2", cai, svi, Comparator.GREATER_THAN, 0, TradeAction.OPEN, OrderSide.buy, MarketUpdateType.PRICE);
        definition.addEnterTradeCondition(condition);
        Robot robot = new Robot(depot, definition, priceEventBus, taLibService, candleService);
        robot.start();
        Price price = new Price(USD_SEK, new BigDecimal("1.99"), new BigDecimal("2.01"), Instant.now(), Broker.OANDA);
        robot.accept(Event.wrap(price));
        Mockito.verify(depot).openTrade(USD_SEK, OrderSide.buy, new BigDecimal("0.02"), price, robot.id);
    }
}
