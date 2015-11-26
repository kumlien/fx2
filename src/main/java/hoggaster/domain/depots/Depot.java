package hoggaster.domain.depots;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderSide;

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
     * @param side
     * @param partOfAvailableMargin Max part of available margin to use, 0.02 means 2%
     * @param marketUpdate
     * @param robotId
     */
    OrderResponse sendOrder(CurrencyPair currencyPair, OrderSide side, BigDecimal partOfAvailableMargin, MarketUpdate marketUpdate, String robotId);
}
