package hoggaster.domain.prices;

import hoggaster.domain.CurrencyPair;

import java.util.Optional;

/**
 * Created by svante.kumlien on 14.10.15.
 */
public interface PriceService {

    Optional<Price> getLatestPriceForCurrencyPair(CurrencyPair currencyPair);

    void store(Price price);
}
