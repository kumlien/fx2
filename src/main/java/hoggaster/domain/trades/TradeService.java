package hoggaster.domain.trades;

import java.util.Collection;

/**
 * Created by svante2 on 2015-11-15.
 */
public interface TradeService {

    Trade createNewTrade(Trade trade);

    void updateTrade(Trade trade);

    Trade findTradeById(String id);

    Trade findTradeByBrokerId(String brokerId);

    Collection<Trade> getOpenTrades(String depotId);

    //Paging...
    Collection<Trade> getClosedTrades(String depotId);

}
