package hoggaster.domain.trades;

import hoggaster.domain.CurrencyPair;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author svante
 */
public interface TradeService {

    Trade saveNewTrade(Trade trade);

    void updateTrade(Trade trade);

    Trade findTradeById(String id);

    Trade findTradeByBrokerId(String brokerId);

    Collection<Trade> getOpenTrades(String depotId);

    Collection<Trade> findByInstrumentAndRobotId(CurrencyPair instrument, String robotId);


    //Paging...
    Collection<Trade> getClosedTrades(String depotId);

    CloseTradeResponse closeTrade(Trade trade, String brokerId);

    //Close the trade on the broker side, save the trade to the historic trade collection and sync the depot.
    CompletableFuture<CloseTradeResponse> closeTradeAsync(Trade trade, String brokerAccountId);
}
