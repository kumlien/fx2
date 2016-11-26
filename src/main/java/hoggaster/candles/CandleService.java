package hoggaster.candles;

import hoggaster.domain.CurrencyPair;
import hoggaster.rules.indicators.candles.CandleStickGranularity;

import java.util.List;

public interface CandleService {


    /**
     * Get the latest stored candles if available. If not avai
     *
     * @param currencyPair
     * @param granularity
     * @param dataPointsNeeded
     * @return A {@link List} of {@link Candle}s
     */
    List<Candle> getLatestCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, int dataPointsNeeded);

    /**
     * Fetch the last candle from the broker
     *
     * @param currencyPair
     * @param granularity
     * @return A {@link List} of fetched {@link Candle}s.
     */
    Candle fetchAndSaveLastCompleteCandle(CurrencyPair currencyPair, CandleStickGranularity granularity);


    /**
     * Fetch historic candles (synchronous...)
     *
     * @param currencyPair
     * @param granularity
     * @return The number of fetched candles.
     */
    int fetchAndSaveHistoricCandles(CurrencyPair currencyPair, CandleStickGranularity granularity);

}
