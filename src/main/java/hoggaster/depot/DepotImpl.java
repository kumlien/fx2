package hoggaster.depot;

import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.NoSuchCurrencyPairException;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.prices.Price;
import hoggaster.prices.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Used to handle logic for a depot.
 * Not so found of this approach, splitting persistent stuff and business logic
 *
 * Created by svante2 on 2015-10-11.
 */
public class DepotImpl implements Depot {


    private static final Logger LOG = LoggerFactory.getLogger(DepotImpl.class);
    private static final BigDecimal UPPER_BOUND_FACTOR = new BigDecimal("1.01");

    private final String dbDepotId;

    private final DepotService depotService;

    // The service we use to deal with orders
    private final OrderService orderService;

    private final PriceService priceService;

    public DepotImpl(String dbDepotId, OrderService orderService, DepotService depotService, PriceService priceService) {
        this.priceService = priceService;
        Objects.requireNonNull(depotService.findDepotById(dbDepotId), "Unable to find a depot with id '" + dbDepotId + "'");
        this.depotService = depotService;
        this.dbDepotId = dbDepotId;
        this.orderService = orderService;
    }


    @Override
    public void sell(CurrencyPair currencyPair, String robotId) {
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        LOG.info("We are told by robot '{}' to sell {}",robotId, currencyPair);
        if (!dbDepot.ownThisInstrument(currencyPair)) {
            LOG.info("Nahh, we don't own {} yet...", currencyPair.name());
            return;
        }

        LOG.info("Ooops, we should sell what we got of {}!", currencyPair.name());
    }

    @Override
    /**
     * Buy something...
     * First -  for now, check if we already own the currencyPair, in that case we bail out.
     * Second - Calculate the value of the order in the depot currency (dollar for us). For now we aim to buy for 2% of the available margin.
     * Third - Check if the order value would push the available margin below 50% of the balance
     * Fourth -
     */
    public void buy(CurrencyPair currencyPair, BigDecimal percentageOfAvailableMargin, MarketUpdate marketUpdate, String robotId) {
        LOG.info("We are told by robot '{}' to spend {} of available margin on buying {}",robotId, percentageOfAvailableMargin, currencyPair);
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        if (dbDepot.ownThisInstrument(currencyPair)) {
            LOG.warn("Unable to buy since we already own {}, only buy once...", currencyPair.name());
            return;
        }

        //TODO Check for margin below 50%
        BigDecimal marginAvailable = dbDepot.getMarginAvailable();
        //int maxUnits = calculateMaxUnitsWeCanBuy(Currency.getInstance(dbDepot.getCurrency()), )

        //TODO For now we try to buy for 2% of available margin
        final BigDecimal maxAmountToBuyFor = marginAvailable.multiply(new BigDecimal(0.02));

        BigDecimal balance = dbDepot.getBalance();


        LOG.info("Ooops, we should buy since we don't own any {} yet!", currencyPair.name());
        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), currencyPair, 1000L, OrderSide.buy, OrderType.market, null, null);
        order.setUpperBound(calculateUpperBound(marketUpdate));
        OandaOrderResponse response = orderService.sendOrder(order);

        LOG.info("Order away and we got n response! {}", response);
    }


    private static BigDecimal calculateUpperBound(MarketUpdate marketUpdate) {
        if(marketUpdate instanceof Price) {
            return ((Price) marketUpdate).ask.multiply(UPPER_BOUND_FACTOR);
        }
        if(marketUpdate instanceof Candle) {
            return ((Candle) marketUpdate).closeAsk.multiply(UPPER_BOUND_FACTOR);
        }
        throw new RuntimeException("Mehhh..." + marketUpdate.getClass().getSimpleName());
    }


    /**
     * This calculation uses the following formula:
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
     *
     *
     */
    public static int calculateMaxUnitsWeCanBuy(Currency homeCurrency, Currency baseCurrency, BigDecimal marginAvailable, BigDecimal marginRatio, PriceService priceService) {
        CurrencyPair currencyPair = null;
        boolean isInverse = false;
        try {
            currencyPair = CurrencyPair.ofBaseAndQuote(baseCurrency, homeCurrency);
        } catch (NoSuchCurrencyPairException e){
            LOG.info("No currency pair found: {}, try with the inverse...", e.getMessage());
            try {
                currencyPair = CurrencyPair.ofBaseAndQuote(homeCurrency, baseCurrency);
            } catch (NoSuchCurrencyPairException ee) {
                LOG.error("Unable to find a currency pair (not even the inverse one) for {} and {} ({}).", homeCurrency, baseCurrency, e.getMessage());
                throw new RuntimeException(ee);
            }
            isInverse = true;
        }
        //get the price...
        final Price lastPrice = priceService.getLatestPriceForCurrencyPair(currencyPair);
        return 0;
    }
}
