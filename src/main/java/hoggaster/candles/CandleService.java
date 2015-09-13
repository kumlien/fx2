package hoggaster.candles;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.time.Instant;
import java.util.List;

public interface CandleService {

    
    /**
     * Get the latest stored candles. 
     * 
     * @param instrument
     * @param granularity
     * @param dataPointsNeeded
     * 
     * @return A {@link List} of {@link Candle}s
     */
    List<Candle> getLatestCandles(Instrument instrument, CandleStickGranularity granularity, int dataPointsNeeded);

    /**
     * Fetch the specified number of candles from the broker
     * 
     * @param instrument
     * @param granularity
     * @param number
     * @return A {@link List} of fetched {@link Candle}s.
     */
    List<Candle> fetchAndSaveNewCandles(Instrument instrument, CandleStickGranularity granularity, Integer number);
    
    
    /**
     * Fetch historic candles (synchronous...)
     * 
     * @param instrument
     * @param granularity
     * @param startDate
     * @param endDate
     * @return The number of fetched candles.
     */
    int fetchAndSaveHistoricCandles(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate);

}
