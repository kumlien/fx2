package hoggaster.depot;

import com.google.common.collect.Sets;
import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.brokers.Broker;
import hoggaster.prices.PriceService;
import hoggaster.rules.indicators.CandleStickGranularity;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Created by svante2 on 2015-10-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class DepotImplTest extends TestCase {

    private Depot depot;

    @Mock
    private OrderService orderService;

    @Mock
    private DepotService depotService;

    @Mock
    private PriceService priceService;

    static final String DEPOT_ID = "Test dbDepot";

    @Before
    public void setUp() throws Exception {
        //DbDepot newDepot = new DbDepot(user.getId(), name, broker, brokerDepot.name, brokerId, brokerDepot.marginRate, brokerDepot.currency, brokerDepot.balance, brokerDepot.unrealizedPl, brokerDepot.realizedPl, brokerDepot.marginUsed, brokerDepot.marginAvail, brokerDepot.openTrades, brokerDepot.openOrders, Instant.now());
        DbDepot dbDepot = new DbDepot(DEPOT_ID, "USER_ID", "The depot name", Broker.OANDA, Sets.newHashSet(), Sets.newHashSet(), "9678914", new BigDecimal(100000.0), new BigDecimal(0.05), Currency.getInstance("USD"),"Primary", new BigDecimal(0.0), new BigDecimal(0.0), new BigDecimal(0.0), new BigDecimal(100000.0), 0, 0, Instant.now(), true, DbDepot.Type.DEMO);
        Mockito.when(depotService.findDepotById(eq(DEPOT_ID))).thenReturn(dbDepot);
        depot = new DepotImpl(dbDepot.getId(), orderService, depotService, priceService);
    }

    @Test
    public void testSell() throws Exception {

    }

    @Test
    public void testBuy() throws Exception {
        CurrencyPair cp = CurrencyPair.USD_SEK;
        Candle candle = new Candle(cp,Broker.OANDA, CandleStickGranularity.END_OF_DAY,Instant.now(),10.0, 11.0, 20.0, 21.0, 5.0, 6.0, 18.0, 19.0, 1000, true);
        depot.buy(cp, new BigDecimal(0.2), candle, "robot_id");
        Mockito.verify(orderService).sendOrder(any(OrderRequest.class));
    }

    public void testCalculateMaxUnitsWeCanBuy() throws Exception {

    }
}