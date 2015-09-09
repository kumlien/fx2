package hoggaster.robot;

import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import com.codahale.metrics.annotation.Timed;

@Service
public class MovingAverageServiceImpl {

    static final long ONE_MINUTE = 60L * 1000;

    static final long ONE_HOUR = ONE_MINUTE * 60;

    static final long ONE_DAY = ONE_HOUR * 24;

    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageServiceImpl.class);

    private final CandleService candleService;

    private final EventBus candleEventBus;

    private static boolean initializedMinuteCandles = false; // Nice... But it
							     // doesn't work to
							     // pre-fetch in
							     // postconstruct...

    private static boolean initializedDayCandles = false;

    @Autowired
    public MovingAverageServiceImpl(CandleService candleService, EventBus candleEventBus) {
	this.candleService = candleService;
	this.candleEventBus = candleEventBus;
    }

    /*
     * Get the one minute candles wtf is this method doing in this class??
     */
    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 5000)
    @Timed
    public void fetchMinuteCandles() {
	LOG.info("About to fetch one minute candles");
	int numberOfCandles = 1;
	try {
	    if (!initializedMinuteCandles) {
		numberOfCandles = 200;
		initializedMinuteCandles = true;
	    }

	    List<Candle> candles = getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity.MINUTE, Instant.now(), numberOfCandles);
	    LOG.info("Got {} one minute candles", candles.size());
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }

    /*
     * Get the daily candles
     * 
     * TODO this one needs to take into account the different opening/closing
     * times for different instruments (and possibly in combination with
     * different brokers) for now we hard code it to open at 17:00:00 and close
     * at 16:59:59 New York time, that is, we record the day candle at this
     * point.
     */
    @Scheduled(cron = "0 0 17 * * MON-FRI")
    @Timed
    public void publishDayCandles() {
	LOG.info("About to fetch one day candles");
	try {
	    int numberOfCandles = 1;
	    if (!initializedDayCandles) {
		numberOfCandles = 200;
		initializedDayCandles = true;
	    }
	    List<Candle> candles = getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity.END_OF_DAY, Instant.now(), numberOfCandles);
	    LOG.info("Done fetching one day candles, got {} of them.", candles.size());
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }

    /*
     * Call the oanda api to get candles.
     */
    private List<Candle> getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity granularity, Instant end, Integer number) throws UnsupportedEncodingException {
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);
	final List<Candle> allCandles = new ArrayList<>();

	// Consumer used to handle one instrument
	Consumer<Instrument> ic = instrument -> {
	    try {
		List<Candle> candles = candleService.getCandles(instrument, granularity, number);
		// Set<Candle> candles = getCandlesForOneInstrument(instrument,
		// granularity, end, number);
		allCandles.addAll(candles);
		candles.forEach(bac -> {
		    candleEventBus.notify("candles." + instrument, Event.wrap(bac));
		});
		LOG.debug("{} {} candles saved and sent to event bus for instrument {} ({})", candles.size(), granularity, instrument, candles.isEmpty() ? null : candles.iterator().next());
	    } catch (Exception e) {
		LOG.error("Error fetching {} candles", granularity, e);
	    }
	};

	// Attach consumers
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);

	Arrays.asList(Instrument.values()).forEach(i -> publisher.onNext(i));
	publisher.onComplete();
	// Sort them by time
	Collections.sort(allCandles, new Comparator<Candle>() {
	    @Override
	    public int compare(Candle o1, Candle o2) {
		if (o1 == null) {
		    return -1;
		}
		if (o2 == null) {
		    return 1;
		}
		return o1.time.compareTo(o2.time);
	    }
	});
	return allCandles;
    }
}
