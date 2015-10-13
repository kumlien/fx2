package hoggaster.depot;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.oanda.responses.OandaOrderResponse;
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

    private final String dbDepotId;

    private final DepotService depotService;

    // The service we use to deal with orders
    private final OrderService orderService;

    private final PriceService priceService;

    public DepotImpl(String dbDepotId, OrderService orderService, DepotService depotService) {
        Objects.requireNonNull(depotService.findDepotById(dbDepotId), "Unable to find a depot with id '" + dbDepotId + "'");
        this.depotService = depotService;
        this.dbDepotId = dbDepotId;
        this.orderService = orderService;
    }


    @Override
    public void sell(CurrencyPair currencyPair, int requestedUnits, String robotId) {
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        LOG.info("We are told by robot {} to sell {}",robotId, currencyPair);
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
    public void buy(CurrencyPair currencyPair, int requestedUnits, MarketUpdate marketUpdate, String robotId) {
        LOG.info("We are told by robot {} to buy {}",robotId, currencyPair);
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        if (dbDepot.ownThisInstrument(currencyPair)) {
            LOG.warn("Unable to buy since we already own {}, only buy once...", currencyPair.name());
            return;
        }

        //TODO Check for margin below 50%
        BigDecimal marginAvailable = dbDepot.getMarginAvailable();
        final BigDecimal maxAmountToBuyFor = marginAvailable.multiply(new BigDecimal(0.02));

        //TODO For now we try to buy for 2% of depot value
        BigDecimal balance = dbDepot.getBalance();


        LOG.info("Ooops, we should buy since we don't own any {} yet!", currencyPair.name());
        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), currencyPair, 1000L, OrderSide.buy, OrderType.market, null, null);
        OandaOrderResponse response = orderService.sendOrder(order);
        LOG.info("Order away and we got an response! {}", response);
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
    public static int calculateMaxUnitsWeCanBuy(Currency homeCurrency, Currency baseCurrency, BigDecimal marginAvailable, BigDecimal marginRatio) {
        CurrencyPair baseAndHome = CurrencyPair.ofBaseAndQuote(baseCurrency,homeCurrency);
        get the price...
        return 0;
    }
}