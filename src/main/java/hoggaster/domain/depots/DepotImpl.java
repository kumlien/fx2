package hoggaster.domain.depots;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.orders.CreateOrderResponse;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.domain.trades.TradeService;
import hoggaster.oanda.exceptions.TradingHaltedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Currency;
import java.util.Objects;

/**
 * Used to handle logic for a depots.
 * Not so found of this approach, splitting persistent stuff and business logic
 * <p>
 * Created by svante2 on 2015-10-11.
 */
public class DepotImpl implements Depot {


    private static final Logger LOG = LoggerFactory.getLogger(DepotImpl.class);

    //Use this factor to calculate upper bound for sendOrder orders
    private static final BigDecimal UPPER_BOUND_FACTOR = new BigDecimal("1.01");

    //Don't sendOrder if margin would go below this threshold as part of balance
    private static final BigDecimal MARGIN_BALANCE_THRESHOLD = new BigDecimal("0.5");

    //Never spend more thant 5% of current available margin
    private static final BigDecimal MAX_PART_OF_MARGIN_TO_SPEND = new BigDecimal("0.05");

    private static final Duration MAX_TIME_SINCE_LAST_DEPOT_SYNC = Duration.ofMinutes(5);

    private final String dbDepotId;

    private final DepotService depotService;

    // The service we use to deal with orders
    private final BrokerConnection brokerConnection;

    private final PriceService priceService;

    private final TradeService tradeService;

    public DepotImpl(String dbDepotId, BrokerConnection brokerConnection, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.priceService = priceService;
        this.tradeService = tradeService;
        Objects.requireNonNull(depotService.findDepotById(dbDepotId), "Unable to find a depots with id '" + dbDepotId + "'");
        this.depotService = depotService;
        this.dbDepotId = dbDepotId;
        this.brokerConnection = brokerConnection;
    }


    @Override
    public void closeTrade(CurrencyPair currencyPair, String robotId) {
        Preconditions.checkArgument(currencyPair != null, "No currency pair is provided");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(robotId), "No robot id is specified");
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        LOG.info("We are told by robot '{}' to close trade for currency pair {}", robotId, currencyPair);
        Collection<Trade> trades = tradeService.findByInstrumentAndRobotId(currencyPair, robotId);
        if(trades == null || trades.size() != 1) {
            LOG.warn("Asked by robot {} to close trade for instrument {} but no matching open trade found!");
            return;
        }
        Trade tradeToClose = trades.iterator().next();
        CloseTradeResponse response = brokerConnection.closeTrade(tradeToClose, dbDepot.brokerId);


    }

    @Override
    /**
     * Buy something...
     * First -  for now, check if we already own the currencyPair, in that case we bail out.
     * Second - Calculate the value of the order in the depots currency (dollar for us). For now we aim to sendOrder for 2% of the available margin.
     * Third - Check if the order value would push the available margin below 50% of the balance
     * Fourth -
     *
     * TODO Refactor MarketUpdate -> BigDecimal ('triggerPrice' or something along those lines)
     * TODO Throw exceptions instead of returning null
     */
    public CreateOrderResponse openTrade(CurrencyPair currencyPair, OrderSide side, BigDecimal partOfAvailableMargin, MarketUpdate marketUpdate, String robotId) {
        LOG.info("We are told by robot '{}' to {} {} of available margin on {}", robotId, side, partOfAvailableMargin, currencyPair);
        Preconditions.checkArgument(StringUtils.hasText(robotId), "No robotId specified");
        Preconditions.checkArgument(currencyPair != null, "The currency pair must not be null");
        Preconditions.checkArgument(partOfAvailableMargin != null, "The partOfAvailableMargin must not be null");
        Preconditions.checkArgument(partOfAvailableMargin.compareTo(BigDecimal.ZERO) > 0, "The partOfAvailableMargin must not be > 0");
        Preconditions.checkArgument(partOfAvailableMargin.compareTo(MAX_PART_OF_MARGIN_TO_SPEND) <= 0, "The partOfAvailableMargin must not be > " + MAX_PART_OF_MARGIN_TO_SPEND);

        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        //Don't allow the trade if the last sync is too old
        if(dbDepot.getLastSynchronizedWithBroker() == null || dbDepot.getLastSynchronizedWithBroker().isBefore(Instant.now().minus(MAX_TIME_SINCE_LAST_DEPOT_SYNC))) {
            LOG.warn("Unable to sendOrder since the depots hasn't been synced with the broker ({}) since {}", dbDepot.broker, dbDepot.getLastSynchronizedWithBroker());
            return null;
        }

        if(!dbDepot.getLastSyncOk()) {
            LOG.warn("Unable to sendOrder since the last sync attempt (at {}) with the broker ({}) was unsuccessful", dbDepot.getLastSynchronizedWithBroker(), dbDepot.broker);
            return null;
        }

        if (dbDepot.hasOpenPositionForInstrument(currencyPair)) {
            LOG.warn("Unable to sendOrder since we already own {}, only sendOrder once...", currencyPair.name());
            return null;
        }
        BigDecimal xRate = getCurrentRate(dbDepot.currency, currencyPair.baseCurrency, priceService);
        BigDecimal maxUnits = calculateMaxUnitsWeCanBuy(dbDepot, currencyPair.baseCurrency, xRate);
        final BigDecimal realUnits = maxUnits.multiply(partOfAvailableMargin, MathContext.DECIMAL32);

        //TODO Check for margin below 50%
        BigDecimal newMarginAsPartOfBalance = getNewMarginAsPartOfBalance(dbDepot, realUnits.multiply(xRate, MathContext.DECIMAL32).multiply(dbDepot.getMarginRate(), MathContext.DECIMAL32));
        if (newMarginAsPartOfBalance.compareTo(MARGIN_BALANCE_THRESHOLD) < 0) {
            LOG.warn("Sorry, no sendOrder since the margin/current balance would drop below the specified threshold (units: {}, xRate: {}, threshold: {}, current balance: {}", realUnits, xRate, MARGIN_BALANCE_THRESHOLD, dbDepot.getBalance());
            return null;
        }

        LOG.info("The number of units we will sendOrder (maxUnits: {} * partOfAvailableMaring: {}) is {}", maxUnits.longValue(), partOfAvailableMargin, realUnits);

        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), currencyPair, realUnits.longValue(), side, OrderType.market, null, null);
        if(marketUpdate != null) { //marketUpdate might be null if triggered interactively
            order.setUpperBound(calculateUpperBound(marketUpdate));
        }
        CreateOrderResponse response = null;
        try {
            response = brokerConnection.sendOrder(order);
            LOG.info("Order away and we got a response! {}", response);
            if (response != null && response.tradeWasOpened()) {
                Trade newTrade = response.getOpenedTrade(dbDepotId, robotId).get();
                dbDepot.tradeOpened(newTrade);
                LOG.info("Trade opened: {}", newTrade);
            } else {
                LOG.warn("No trade opened, better check open orders!");
            }
            depotService.save(dbDepot);
            depotService.syncDepot(dbDepot);
        } catch (TradingHaltedException the) {
            LOG.info("No order placed since the trading is halted for {} ({})", currencyPair, the.getMessage());
        }

        return response;
    }


    //Used to calculate the max price we are willing to pay.
    private static BigDecimal calculateUpperBound(MarketUpdate marketUpdate) {
        if (marketUpdate instanceof Price) {
            return ((Price) marketUpdate).ask.multiply(UPPER_BOUND_FACTOR);
        }
        if (marketUpdate instanceof Candle) {
            return ((Candle) marketUpdate).closeAsk.multiply(UPPER_BOUND_FACTOR);
        }
        throw new RuntimeException("Mehhh..." + marketUpdate.getClass().getSimpleName());
    }


    /**
     * This calculation uses the following formula:
     * <p>
     * Margin Available * (margin ratio) / ({BASE}/{HOME Currency} Exchange Rate)
     * For example, suppose:
     * Home Currency: USD
     * Currency Pair: GBP/CHF
     * Margin Available: 100
     * Margin Ratio : 20:1
     * Base / Home Currency: GBP/USD = 1.584
     * <p>
     * Then,
     * Units = (100 * 20) / 1.584
     * Units = 1262
     */
    static BigDecimal calculateMaxUnitsWeCanBuy(DbDepot dbDepot, Currency baseCurrency, BigDecimal xRate) {
        Currency homeCurrency = dbDepot.currency;
        BigDecimal marginRatio = dbDepot.getMarginRate();
        LOG.info("Calculating max units to sendOrder for home curreny: {}, base currency: {}, margin available: {} and margin ratio: {}", homeCurrency, baseCurrency, dbDepot.getMarginAvailable(), marginRatio);

        BigDecimal totalAmount = dbDepot.getMarginAvailable().divide(marginRatio, MathContext.DECIMAL32);
        LOG.info("Total amount to sendOrder for (margin available divided by margin ratio) in {} is {}", homeCurrency, totalAmount.longValue());
        BigDecimal totalUnits = totalAmount.divide(xRate, MathContext.DECIMAL32);
        LOG.info("Total units we can sendOrder ({}/{}) is {}", totalAmount.longValue(), xRate, totalUnits.longValue());

        return totalUnits;
    }


    /**
     * Current requirement says that a new order must not push the (available margin / margin ration) below
     * 50% of balance
     * See ticket 19 on github.
     *
     * @return The margin available divided by balance given the specified amount is used as margin.
     */
    static BigDecimal getNewMarginAsPartOfBalance(DbDepot dbDepot, BigDecimal marginAmountNeeded) {
        LOG.info("Margin amount needed for this trade is {}", marginAmountNeeded);
        final BigDecimal balance = dbDepot.getBalance();
        LOG.info("Current balance is {}", balance);
        final BigDecimal newMarginAvailable = dbDepot.getMarginAvailable().subtract(marginAmountNeeded, MathContext.DECIMAL32);
        LOG.info("New margin available after transactions would be {}", newMarginAvailable);
        final BigDecimal marginAvailableDividedByBalance = newMarginAvailable.divide(balance, MathContext.DECIMAL32);

        LOG.info("Margin available divided by balance will be {} after this transactions", marginAvailableDividedByBalance);

        return marginAvailableDividedByBalance;
    }



    static BigDecimal getCurrentRate(Currency homeCurrency, Currency baseCurrency, PriceService priceService) {
        if (homeCurrency == baseCurrency) return BigDecimal.ONE;
        CurrencyPair baseAndHomePair;
        baseAndHomePair = CurrencyPair.ofBaseAndQuote(baseCurrency, homeCurrency);

        //get the price...
        final Price lastPriceForBaseAndHome = Objects.requireNonNull(priceService.getLatestPriceForCurrencyPair(baseAndHomePair).get(), "No price available for " + baseAndHomePair);
        return lastPriceForBaseAndHome.bid;
    }
}
