package hoggaster.domain.orders;

import hoggaster.domain.trades.Trade;

import java.util.Optional;

public interface OrderResponse {

    boolean tradeWasOpened();

    Optional<Trade> getOpenedTrade(String depotId, String robotId);


}
