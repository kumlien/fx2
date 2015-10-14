package hoggaster.prices;

import hoggaster.domain.CurrencyPair;

import java.time.Instant;

/**
 * Created by svante.kumlien on 14.10.15.
 */
public interface PriceService {

    Price getLatestPriceForCurrencyPair(CurrencyPair currencyPair);

    Price getLatestPriceForCurrencyPairAfterDate(CurrencyPair currencyPair, Instant instant);

    Price store(Price price);
}
