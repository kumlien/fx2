package hoggaster.domain.depot;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.orders.OrderResponse;

import java.math.BigDecimal;

/**
 * Created by svante2 on 2015-10-11.
 */
public interface Depot {

    /**
     *
     * @param currencyPair
     * @param robotId
     */
    void sell(CurrencyPair currencyPair, String robotId);

    /**
     *
     * @param currencyPair
     * @param percentageOfAvailableMargin
     * @param marketUpdate
     * @param robotId
     */
    OrderResponse buy(CurrencyPair currencyPair, BigDecimal percentageOfAvailableMargin, MarketUpdate marketUpdate, String robotId);
}
