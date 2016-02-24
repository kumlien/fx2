package hoggaster.domain.prices;

import hoggaster.domain.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by svante.kumlien on 14.10.15.
 *
 * TODO If not found in cache, look up from some broker
 */
@Service
public class PriceServiceImpl implements PriceService {

    private static final Logger LOG = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final Map<CurrencyPair, Price> cache = new ConcurrentHashMap<>();

    @Autowired
    public PriceServiceImpl(@Qualifier("priceEventBus") EventBus priceEventBus) {
        priceEventBus.on(Selectors.matchAll(), e -> {
            cache.put(((Price)e.getData()).currencyPair, (Price) e.getData());
        });
    }


    @Override
    public Price getLatestPriceForCurrencyPair(CurrencyPair currencyPair) {
        return cache.get(currencyPair);
    }

    @Override
    public void store(Price price) {
        cache.put(price.currencyPair, price);
    }
}
