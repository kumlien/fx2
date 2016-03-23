package hoggaster.domain.orders;

import hoggaster.domain.trades.Trade;

import java.util.Optional;

/**
 * The order response can carry somewhat different info depending on the request... Might result in an opened trade, some closed/reduced trades or in
 * no trade at all but in an open order.
 */
public interface OrderResponse {

    boolean tradeWasOpened();

    Optional<Trade> getOpenedTrade(String depotId, String robotId);





}
