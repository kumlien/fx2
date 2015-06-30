package hoggaster.candles;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BidAskCandleRepo extends MongoRepository<BidAskCandle, String> {
	
    /**
     * Get a number of candles order by date (desc).
     * 
     * @param instrument
     * @param granularity
     * @param pageable
     * 
     * @return The list of candles 
     */
    List<BidAskCandle> findByInstrumentAndGranularityOrderByTimeDesc(Instrument instrument, CandleStickGranularity granularity, Pageable pageable);
}
