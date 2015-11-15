package hoggaster.prices;

import hoggaster.domain.CurrencyPair;

/**
 * Created by svante.kumlien on 14.10.15.
 */
public interface PriceService {

    Price getLatestPriceForCurrencyPair(CurrencyPair currencyPair);

    void store(Price price);
}
