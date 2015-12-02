package hoggaster.domain.depots;

import com.google.common.collect.Sets;
import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.NoSuchCurrencyPairException;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.TradeService;
import hoggaster.oanda.responses.OandaCreateOrderResponse;
import hoggaster.rules.indicators.CandleStickGranularity;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by svante2 on 2015-10-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class DepotImplTest extends TestCase {

    public static final String EXTERNAL_DEPOT_ID = "9678914";
    public static final BigDecimal MARGIN_RATE = new BigDecimal("0.05");
    public static final BigDecimal UNREALIZED_PL = new BigDecimal("0.0");
    public static final BigDecimal REALIZED_PL = new BigDecimal("0.0");
    public static final BigDecimal MARGIN_USED = new BigDecimal("0.0");
    public static final BigDecimal MARGIN_AVAILABLE = new BigDecimal("100000.0");
    public static final BigDecimal BALANCE = new BigDecimal("100000");
    public static final String DEPOT_ID = "Test dbDepot";
    public static final CurrencyPair cp = CurrencyPair.USD_SEK;
    private Depot depot;

    @Mock
    private OrderService orderService;

    @Mock
    private DepotService depotService;

    @Mock
    private PriceService priceService;

    @Mock
    private TradeService tradeService;

    @Captor
    private ArgumentCaptor<OrderRequest> rac;

    DbDepot dbDepot;



    @Before
    public void setUp() throws Exception {
        dbDepot = new DbDepot(
                DEPOT_ID, "USER_ID", "The depots name", Broker.OANDA, Sets.newHashSet(),
                EXTERNAL_DEPOT_ID, BALANCE, MARGIN_RATE, Currency.getInstance("USD"),"Primary",
                UNREALIZED_PL, REALIZED_PL, MARGIN_USED, MARGIN_AVAILABLE, 0, 0, Instant.now(), true, DbDepot.Type.DEMO);
        Mockito.when(depotService.findDepotById(eq(DEPOT_ID))).thenReturn(dbDepot);

        depot = new DepotImpl(dbDepot.getId(), orderService, depotService, priceService, tradeService);
    }

    @Test
    public void testSell() throws Exception {

    }

    @Test
    public void testBuy() throws Exception {
        Candle candle = new Candle(cp,Broker.OANDA, CandleStickGranularity.END_OF_DAY,Instant.now(), new BigDecimal("10.0"), new BigDecimal("11.0"), new BigDecimal("20.0"), new BigDecimal("21.0"), new BigDecimal("5.0"), new BigDecimal("6.0"), new BigDecimal("18.0"), new BigDecimal("19.0"), 1000, true);
        Mockito.when(orderService.sendOrder(any(OrderRequest.class))).thenReturn(new OandaCreateOrderResponse(cp, new BigDecimal("19.0"), Instant.now(), null, null, null, null));

        depot.sendOrder(cp, OrderSide.buy, new BigDecimal("0.02"), candle, "robot_id");
        OrderRequest expectedRequest = new OrderRequest(EXTERNAL_DEPOT_ID, cp, 40000L, OrderSide.buy, OrderType.market, Instant.now(), null);
        expectedRequest.setUpperBound(candle.closeAsk.multiply(new BigDecimal("1.01"))); //TODO This value should be fetched from the robot or depots definition


        verify(orderService).sendOrder(rac.capture());
        OrderRequest request = rac.getValue();

        assertEquals(expectedRequest.currencyPair, request.currencyPair);
        assertTrue(request.expiry == null); //No expiry for market orders
        assertEquals(expectedRequest.externalDepotId, request.externalDepotId);
        assertEquals(expectedRequest.price, request.price); //No price for a market order
        assertEquals(expectedRequest.getLowerBound(), null); //No lower bound for a sendOrder order
        assertEquals(expectedRequest.getUpperBound(), request.getUpperBound()); //Upper bound should match
        assertEquals(expectedRequest.getStopLoss(), request.getStopLoss());
        assertEquals(expectedRequest.getTakeProfit(), request.getTakeProfit());
        assertEquals(expectedRequest.units, request.units);

        System.out.println(request);

    }


    @Test
    public void testBuyBreakMarginRule() throws Exception {
        dbDepot.setMarginAvailable(BigDecimal.ZERO);
        depot.sendOrder(cp, OrderSide.buy, new BigDecimal("0.05"), null, "robot_id");
        Mockito.verifyZeroInteractions(orderService);
    }


    /**
     * This calculation uses the following formula:
     *
     * Margin Available * (margin ratio) / ({BASE}/{HOME Currency} Exchange Rate)
     * For example, suppose:
     * Home Currency: USD
     * Currency Pair: GBP/CHF
     * Margin Available: 100
     * Margin Ratio : 20:1
     * Base / Home Currency: GBP/USD = 1.584
     *
     * Then,
     * Units = (100 * 20) / 1.584
     * Units = 1262
     */
    @Test
    public void testCalculateMaxUnitsWeCanBuy1() throws Exception {
        Currency homeCurrency = Currency.getInstance("USD");
        Currency baseCurrency = Currency.getInstance("GBP");
        CurrencyPair cpToBuy = CurrencyPair.GBP_CHF;
        CurrencyPair cpBaseOverHome = CurrencyPair.GBP_USD;
        BigDecimal marginAvailable = new BigDecimal("100");
        BigDecimal marginRatio = new BigDecimal("0.05");
        BigDecimal bid = new BigDecimal("1.584");
        BigDecimal ask = new BigDecimal("1.600");
        Long expectedUnits = 1262L;
        Price baseOverHomePrice = new Price(cpBaseOverHome, bid, ask, Instant.now(), Broker.OANDA);
        when(priceService.getLatestPriceForCurrencyPair(eq(cpBaseOverHome))).thenReturn(baseOverHomePrice);


        DbDepot dbDepot = new DbDepot(
                DEPOT_ID, "USER_ID", "The depots name", Broker.OANDA, Sets.newHashSet(),
                EXTERNAL_DEPOT_ID, BALANCE, marginRatio, homeCurrency,"Primary",
                UNREALIZED_PL, REALIZED_PL, MARGIN_USED, marginAvailable, 0, 0, Instant.now(), true, DbDepot.Type.DEMO);

        BigDecimal currentRate = DepotImpl.getCurrentRate(dbDepot.currency, baseCurrency, priceService);
        BigDecimal units = DepotImpl.calculateMaxUnitsWeCanBuy(dbDepot, baseCurrency, currentRate);
        assertTrue(expectedUnits == units.longValue());
    }



    /**
     * This calculation uses the following formula:
     *
     * Margin Available * (margin ratio) / ({BASE}/{HOME Currency} Exchange Rate)
     * For example, suppose:
     * Home Currency: EUR
     * Currency Pair: USD/CHF
     * Margin Available: 100
     * Margin Ratio : 20:1
     * Base / Home Currency: USD/EUR = 0.9309 (inverse is 1.0742)
     *
     * Then,
     * Units = (100 * 20) / 0.9309
     * Units = 2148
     */
    @Test(expected = NoSuchCurrencyPairException.class)
    public void testCalculateMaxUnitsWeCanBuyInverse() throws Exception {
        Currency homeCurrency = Currency.getInstance("EUR");
        Currency baseCurrency = Currency.getInstance("USD");
        CurrencyPair cpToBuy = CurrencyPair.USD_CHF;
        CurrencyPair cpBaseOverHomeInverse = CurrencyPair.EUR_USD;
        BigDecimal marginAvailable = new BigDecimal("100");
        BigDecimal marginRatio = new BigDecimal("0.05");
        BigDecimal bid = new BigDecimal("1.0742");
        BigDecimal ask = new BigDecimal("1.100");
        long expectedUnits = 2148L;
        Price baseOverHomePrice = new Price(cpBaseOverHomeInverse, bid, ask, Instant.now(), Broker.OANDA);
        when(priceService.getLatestPriceForCurrencyPair(eq(cpBaseOverHomeInverse))).thenReturn(baseOverHomePrice);

        DbDepot dbDepot = new DbDepot(
                DEPOT_ID, "USER_ID", "The depots name", Broker.OANDA, Sets.newHashSet(),
                EXTERNAL_DEPOT_ID, BALANCE, marginRatio, homeCurrency,"Primary",
                UNREALIZED_PL, REALIZED_PL, MARGIN_USED, marginAvailable, 0, 0, Instant.now(), true, DbDepot.Type.DEMO);
        BigDecimal currentRate = DepotImpl.getCurrentRate(dbDepot.currency, baseCurrency, priceService);
        BigDecimal units = DepotImpl.calculateMaxUnitsWeCanBuy(dbDepot, baseCurrency, currentRate);
        assertEquals(expectedUnits,units.longValue());
    }

}