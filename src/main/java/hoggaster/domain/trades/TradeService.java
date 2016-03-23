package hoggaster.domain.trades;

import hoggaster.domain.CurrencyPair;

import java.util.Collection;

/**
 * Service interface for Trades. To create a trade we first need to send a new {@link hoggaster.domain.orders.OrderRequest}
 *
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
}
