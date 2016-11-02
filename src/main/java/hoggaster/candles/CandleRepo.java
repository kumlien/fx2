package hoggaster.candles;

import hoggaster.domain.CurrencyPair;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CandleRepo extends MongoRepository<Candle, String> {

    /**
     * Get a number of candles order by date (desc).
     *
     * @param currencyPair
     * @param granularity
     * @param pageable
     * @return The list of candles
     */
    List<Candle> findByCurrencyPairAndGranularityOrderByTimeDesc(CurrencyPair currencyPair, CandleStickGranularity granularity, Pageable pageable);

}
