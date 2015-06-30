package hoggaster.robot;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

public interface MovingAverageService {
    
    Double getMA(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints);

    void fetchMinuteCandles();

    void getDayCandles();
}
