package hoggaster.robot;

import hoggaster.candles.CandleService;
import hoggaster.depot.Depot;
import hoggaster.depot.DepotService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.prices.Price;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.conditions.Side;
import hoggaster.rules.conditions.TwoIndicatorCondition;
import hoggaster.rules.indicators.CurrentAskIndicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import hoggaster.talib.TALibService;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
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

        definition = new RobotDefinition("Frekkin robot!", CurrencyPair.USD_SEK, "1");
        Mockito.when(priceEventBus.on(Mockito.any(), Mockito.any())).thenReturn(registration);
    }

    @Test
    public void testSimpleBuyOnPrice() {
        SimpleValueIndicator svi = new SimpleValueIndicator(new BigDecimal("2.0")); //First indicator
        CurrentAskIndicator cai = new CurrentAskIndicator(); //Second indicator
        //Let's compare them in a condition, putting an operator between them
        TwoIndicatorCondition condition = new TwoIndicatorCondition("Buy when ask is > 2", cai, svi, Comparator.GREATER_THAN, 2, Side.BUY, MarketUpdateType.PRICE);
        definition.addBuyCondition(condition);
        RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine().build();
        Robot robot = new Robot(depot, definition, priceEventBus, rulesEngine, taLibService, candleService);
        robot.start();
        Price price = new Price(CurrencyPair.USD_SEK, new BigDecimal("1.99"), new BigDecimal("2.01"), Instant.now(), Broker.OANDA);
        robot.accept(Event.wrap(price));
        Mockito.verify(depot).buy(CurrencyPair.USD_SEK, new BigDecimal(0.2), price, robot.id);
    }
}
