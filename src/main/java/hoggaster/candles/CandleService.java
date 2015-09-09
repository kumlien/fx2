package hoggaster.candles;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.util.List;

public interface CandleService {

    List<Candle> getCandles(Instrument instrument, CandleStickGranularity granularity, int dataPointsNeeded);

}
