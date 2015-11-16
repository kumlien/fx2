package hoggaster.domain.depot;

import hoggaster.candles.Candle;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.NoSuchCurrencyPairException;
import hoggaster.domain.orders.*;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.Trade;
import hoggaster.domain.trades.TradeService;
import hoggaster.oanda.exceptions.TradingHaltedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Currency;
import java.util.Objects;

/**
 * Used to handle logic for a depot.
 * Not so found of this approach, splitting persistent stuff and business logic
 * <p>
 * Created by svante2 on 2015-10-11.
 */
public class DepotImpl implements Depot {


    private static final Logger LOG = LoggerFactory.getLogger(DepotImpl.class);
    private static final BigDecimal UPPER_BOUND_FACTOR = new BigDecimal("1.01");
    private static final BigDecimal MARGIN_BALANCE_THRESHOLD = new BigDecimal("0.5");

    private final String dbDepotId;

    private final DepotService depotService;

    // The service we use to deal with orders
    private final OrderService orderService;

    private final PriceService priceService;

    private final TradeService tradeService;

    public DepotImpl(String dbDepotId, OrderService orderService, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.priceService = priceService;
        this.tradeService = tradeService;
        Objects.requireNonNull(depotService.findDepotById(dbDepotId), "Unable to find a depot with id '" + dbDepotId + "'");
        this.depotService = depotService;
        this.dbDepotId = dbDepotId;
        this.orderService = orderService;
    }


    @Override
    public void sell(CurrencyPair currencyPair, String robotId) {
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        LOG.info("We are told by robot '{}' to sell {}", robotId, currencyPair);
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
     *
     * TODO Refac MarketUpdate -> BigDecimal ('trigger price' or something along those lines)
     */
    public OrderResponse buy(CurrencyPair currencyPair, BigDecimal partOfAvailableMargin, MarketUpdate marketUpdate, String robotId) {
        LOG.info("We are told by robot '{}' to spend {} of available margin on buying {}", robotId, partOfAvailableMargin, currencyPair);
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        if (dbDepot.ownThisInstrument(currencyPair)) {
            LOG.warn("Unable to buy since we already own {}, only buy once...", currencyPair.name());
            return null;
        }
        LOG.info("We should buy since we don't own any {} yet!", currencyPair.name());
        BigDecimal xRate = getCurrentRate(dbDepot.currency, currencyPair.baseCurrency, priceService);
        BigDecimal maxUnits = calculateMaxUnitsWeCanBuy(dbDepot, currencyPair.baseCurrency, xRate);
        final BigDecimal realUnits = maxUnits.multiply(partOfAvailableMargin, MathContext.DECIMAL32);

        //TODO Check for margin below 50%
        BigDecimal newMarginAsPartOfBalance = getNewMarginAsPartOfBalance(dbDepot, realUnits.multiply(xRate, MathContext.DECIMAL32).multiply(dbDepot.getMarginRate(), MathContext.DECIMAL32));
        if (newMarginAsPartOfBalance.compareTo(MARGIN_BALANCE_THRESHOLD) < 0) {
            LOG.warn("Sorry, no buy since the margin/current balance would drop below the specified threshold (units: {}, xRate: {}, threshold: {}, current balance: {}", realUnits, xRate, MARGIN_BALANCE_THRESHOLD, dbDepot.getBalance());
            return null;
        }

        LOG.info("The number of units we will buy ({} * {}) is {}", maxUnits.longValue(), partOfAvailableMargin, realUnits);

        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), currencyPair, realUnits.longValue(), OrderSide.buy, OrderType.market, null, null);
        order.setUpperBound(calculateUpperBound(marketUpdate));
        OrderResponse response = null;
        try {
            response = orderService.sendOrder(order);
            LOG.info("Order away and we got a response! {}", response);
            if (response.tradeWasOpened()) {
                Trade newTrade = response.getOpenedTrade(dbDepotId, robotId).get();
                dbDepot.bought(currencyPair, newTrade.units, newTrade.openPrice);
                tradeService.saveNewTrade(newTrade);
                LOG.info("Trade opened: {}", newTrade);
            } else {
                LOG.warn("No trade opened, better check open orders!");
            }
            depotService.save(dbDepot);
        } catch (TradingHaltedException the) {
            LOG.info("No order placed since the trading is halted for {} ({})", currencyPair, the.getMessage());
        }

        return response;

        //TODO save order/trade here..
    }


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
        LOG.info("Calculating max units to buy for home curreny: {}, base currency: {}, margin available: {} and margin ratio: {}", homeCurrency, baseCurrency, dbDepot.getMarginAvailable(), marginRatio);

        BigDecimal totalAmount = dbDepot.getMarginAvailable().divide(marginRatio, MathContext.DECIMAL32);
        LOG.info("Total amount to buy for (margin available divided by margin ratio) in {} is {}", homeCurrency, totalAmount.longValue());
        BigDecimal totalUnits = totalAmount.divide(xRate, MathContext.DECIMAL32);
        LOG.info("Total units we can buy ({}/{}) is {}", totalAmount.longValue(), xRate, totalUnits.longValue());

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
        LOG.info("New margin available after transaction would be {}", newMarginAvailable);
        final BigDecimal marginAvailableDividedByBalance = newMarginAvailable.divide(balance, MathContext.DECIMAL32);

        LOG.info("Margin available divided by balance will be {} after this transaction", marginAvailableDividedByBalance);

        return marginAvailableDividedByBalance;
    }

    static BigDecimal getCurrentRate(Currency homeCurrency, Currency baseCurrency, PriceService priceService) {
        if (homeCurrency == baseCurrency) return new BigDecimal("1");
        CurrencyPair baseAndHomePair;
        boolean isInverse = false;
        try {
            baseAndHomePair = CurrencyPair.ofBaseAndQuote(baseCurrency, homeCurrency);
        } catch (NoSuchCurrencyPairException e) {
            LOG.info("No currency pair found for {}_{}, try with the inverse...", baseCurrency, homeCurrency, e.getMessage());
            try {
                baseAndHomePair = CurrencyPair.ofBaseAndQuote(homeCurrency, baseCurrency);
            } catch (NoSuchCurrencyPairException ee) {
                LOG.error("Unable to find a currency pair (not even the inverse one) for {} and {} ({}).", homeCurrency, baseCurrency, e.getMessage());
                throw new RuntimeException(ee);
            }
            LOG.info("Found currency pair {}, will use that one in lookup and use inverse x-rate", baseAndHomePair);
            isInverse = true;
        }
        //get the price...
        final Price lastPriceForBaseAndHome = Objects.requireNonNull(priceService.getLatestPriceForCurrencyPair(baseAndHomePair), "No price available for " + baseAndHomePair);
        if (isInverse) {
            return BigDecimal.ONE.divide(lastPriceForBaseAndHome.bid, MathContext.DECIMAL32);
        }
        return lastPriceForBaseAndHome.bid;
    }
}
