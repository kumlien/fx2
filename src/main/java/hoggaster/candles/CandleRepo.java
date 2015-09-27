package hoggaster.candles;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CandleRepo extends MongoRepository<Candle, String> {

    /**
     * Get a number of candles order by date (desc).
     *
     * @param instrument
     * @param granularity
     * @param pageable
     * @return The list of candles
     */
    List<Candle> findByInstrumentAndGranularityOrderByTimeDesc(Instrument instrument, CandleStickGranularity granularity, Pageable pageable);

}
