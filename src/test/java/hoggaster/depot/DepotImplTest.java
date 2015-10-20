package hoggaster.depot;

import com.google.common.collect.Sets;
import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.prices.PriceService;
import hoggaster.rules.indicators.CandleStickGranularity;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

/**
 * Created by svante2 on 2015-10-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class DepotImplTest extends TestCase {

    public static final String EXTERNAL_DEPOT_ID = "9678914";
    public static final BigDecimal MARGIN_RATE = new BigDecimal(0.05);
    public static final BigDecimal UNREALIZED_PL = new BigDecimal(0.0);
    public static final BigDecimal REALIZED_PL = new BigDecimal(0.0);
    public static final BigDecimal MARGIN_USED = new BigDecimal(0.0);
    public static final BigDecimal MARGIN_AVAILABLE = new BigDecimal(100000.0);
    public static final BigDecimal BALANCE = new BigDecimal(100000);
    public static final String DEPOT_ID = "Test dbDepot";
    public static final CurrencyPair cp = CurrencyPair.USD_SEK;
    private Depot depot;

    @Mock
    private OrderService orderService;

    @Mock
    private DepotService depotService;

    @Mock
    private PriceService priceService;

    @Captor
    private ArgumentCaptor<OrderRequest> requestArgumentCaptor;



    @Before
    public void setUp() throws Exception {
        //DbDepot newDepot = new DbDepot(user.getId(), name, broker, brokerDepot.name, brokerId, brokerDepot.marginRate, brokerDepot.currency, brokerDepot.balance, brokerDepot.unrealizedPl, brokerDepot.realizedPl, brokerDepot.marginUsed, brokerDepot.marginAvail, brokerDepot.openTrades, brokerDepot.openOrders, Instant.now());
        DbDepot dbDepot = new DbDepot(DEPOT_ID, "USER_ID", "The depot name", Broker.OANDA, Sets.newHashSet(), Sets.newHashSet(), EXTERNAL_DEPOT_ID, BALANCE, MARGIN_RATE, Currency.getInstance("USD"),"Primary", UNREALIZED_PL, REALIZED_PL, MARGIN_USED, MARGIN_AVAILABLE, 0, 0, Instant.now(), true, DbDepot.Type.DEMO);
        Mockito.when(depotService.findDepotById(eq(DEPOT_ID))).thenReturn(dbDepot);
        Mockito.when(orderService.sendOrder(any(OrderRequest.class))).thenReturn(new OandaOrderResponse(cp,0.0, Instant.now(), null, null, null));
        depot = new DepotImpl(dbDepot.getId(), orderService, depotService, priceService);
    }

    @Test
    public void testSell() throws Exception {

    }

    @Test
    public void testBuy() throws Exception {
        Candle candle = new Candle(cp,Broker.OANDA, CandleStickGranularity.END_OF_DAY,Instant.now(), 10.0, 11.0, 20.0, 21.0, 5.0, 6.0, 18.0, 19.0, 1000, true);
        depot.buy(cp, new BigDecimal(0.2), candle, "robot_id");
        OrderRequest expectedRequest = new OrderRequest(EXTERNAL_DEPOT_ID, cp, 1l, OrderSide.buy, OrderType.market, Instant.now(), null);
        expectedRequest.setUpperBound(candle.closeAsk * 1.01);

        verify(orderService).sendOrder(requestArgumentCaptor.capture());

        assertEquals(expectedRequest.currencyPair, requestArgumentCaptor.getValue().currencyPair);
        assertTrue(requestArgumentCaptor.getValue().expiry == null); //No expiry for market orders
        assertEquals(expectedRequest.externalDepotId, requestArgumentCaptor.getValue().externalDepotId);
        assertEquals(expectedRequest.price, null); //No price for a market order
        assertEquals(expectedRequest.getLowerBound(), null); //No lower bound for a buy order
        assertEquals(expectedRequest.getUpperBound(), requestArgumentCaptor.getValue().getUpperBound()); //

    }

    public void testCalculateMaxUnitsWeCanBuy() throws Exception {

    }

    class OrderRequestArgMatcher extends ArgumentMatcher<OrderRequest> {

        private final OrderRequest expected;

        public OrderRequestArgMatcher(OrderRequest expectedRequest) {
            this.expected = expectedRequest;
        }

        @Override
        public boolean matches(Object req) {
            OrderRequest actual = (OrderRequest) req;
            System.out.println(actual);


            return false;
        }
    }
}