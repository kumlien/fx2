package hoggaster.depot;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;

/**
 * Created by svante2 on 2015-10-11.
 */
public interface Depot {
    void sell(CurrencyPair currencyPair, int requestedUnits, String robotId);

    void buy(CurrencyPair currencyPair, int requestedUnits, MarketUpdate marketUpdate, String robotId);
}
