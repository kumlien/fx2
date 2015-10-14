package hoggaster.prices;

import hoggaster.domain.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Created by svante.kumlien on 14.10.15.
 */
@Service
public class PriceServiceImpl implements PriceService {

    private final PriceRepo priceRepo;

    @Autowired
    public PriceServiceImpl(PriceRepo priceRepo) {
        this.priceRepo = priceRepo;
    }

    @Override
    public Price getLatestPriceForCurrencyPair(CurrencyPair currencyPair) {
        return priceRepo.findByCurrencyPairOrderByTimeDesc(currencyPair);
    }

    @Override
    public Price getLatestPriceForCurrencyPairAfterDate(CurrencyPair currencyPair, Instant instant) {
        return null;
    }

    @Override
    public Price store(Price price) {
        return null;
    }
}
