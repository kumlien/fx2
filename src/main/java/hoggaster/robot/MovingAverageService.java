package hoggaster.robot;

import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MovingAverageService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MovingAverageService.class);
	
	//We need some kind of cache here...
	//Init at start-up from db or oanda if not present
	
	@PostConstruct
	public void initCache() {
		//Read values from db or/and oanda.
		//God knows what the size of this cache will be...
	}

	public Double getMovingAverage(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints) {
		LOG.info("Will try to calculate moving average for {} for granularity {} with {} data points", instrument, granularity, numberOfDataPoints);
		
		
		return 0.0;
	}

}
