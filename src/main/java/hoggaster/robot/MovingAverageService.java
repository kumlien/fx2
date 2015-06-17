package hoggaster.robot;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

public interface MovingAverageService {
    
    Double getMovingAverage(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints);
}
