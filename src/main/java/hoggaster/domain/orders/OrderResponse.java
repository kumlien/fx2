package hoggaster.domain.orders;

import hoggaster.domain.trades.Trade;

import java.util.Optional;

public interface OrderResponse {

    Optional<Trade> getOpenedTrade();


}
