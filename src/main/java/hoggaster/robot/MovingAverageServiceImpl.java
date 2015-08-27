package hoggaster.robot;

import hoggaster.candles.BidAskCandle;
import hoggaster.candles.BidAskCandleRepo;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class MovingAverageServiceImpl implements MovingAverageService {
    
    static final long ONE_MINUTE = 60L * 1000;
    
    static final long ONE_HOUR = ONE_MINUTE * 60;
    
    static final long ONE_DAY = ONE_HOUR * 24;

    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageServiceImpl.class);

    private final BidAskCandleRepo bidAskCandleRepo;

    private final BrokerConnection oanda;
    
    private final EventBus candleEventBus;
    
    private static boolean initializedMinuteCandles = false; //Nice... But it doesn't work to pre-fetch in postconstruct...
    
    private static boolean initializedDayCandles = false;


    @Autowired
    public MovingAverageServiceImpl(BidAskCandleRepo bidAskCandleRepo,@Qualifier("OandaBrokerConnection") BrokerConnection oanda, EventBus candleEventBus) {
	this.bidAskCandleRepo = bidAskCandleRepo;
	this.oanda = oanda;
	this.candleEventBus = candleEventBus;
    }
    
    //TODO Don't read from db all the time...
    @Override
    //@Cacheable("movingAverages")
    //TODO Right now hard coded to use the closeBid from the candles when calculating the average
    public Double getMA(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints) {
	LOG.info("Will try to calculate moving average for {} for granularity {} with {} data points", instrument, granularity, numberOfDataPoints);
	Pageable pageable = new PageRequest(0, numberOfDataPoints);
	List<BidAskCandle> candles = bidAskCandleRepo.findByInstrumentAndGranularityOrderByTimeDesc(instrument, granularity, pageable );
	LOG.info("Got a list: {}", candles.size());
	if(candles.size() < numberOfDataPoints) {
	    try {
		candles = getStoreAndNotifyCandlesForAllInstruments(granularity, Instant.now(), numberOfDataPoints, true, false);
	    } catch (UnsupportedEncodingException e) {
		LOG.error("Error fetching candles",e);
	    }
	}
	double average = candles.stream().mapToDouble(bid -> bid.closeBid).average().getAsDouble();
	LOG.info("Average calculated to {}", average);
	return average;
    }
    

    /*
     * Get the one minute candles
     */
    @Override
    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 5000)
    @Timed
    public void fetchMinuteCandles() {
	LOG.info("About to fetch one minute candles");
	int numberOfCandles = 1;
	try {
	    if(!initializedMinuteCandles) {
		numberOfCandles = 200;
		initializedMinuteCandles = true;
	    }
	    
	    List<BidAskCandle> candles = getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity.MINUTE, Instant.now(), numberOfCandles, true, true);
	    LOG.info("Got {} one minute candles", candles.size() );
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }
    
    /*
     * Get the daily candles
     * 
     * TODO this one needs to take into account the different opening/closing times for different instruments (and possibly in combination with different brokers) 
     * for now we hard code it to open at 17:00:00 and close at 16:59:59 New York time, that is, we record the day candle at this point.
     */
    @Override
    @Scheduled(cron="0 0 17 * * MON-FRI")
    @Timed
    public void fetchDayCandles() {
	LOG.info("About to fetch one day candles");
	try {
	    int numberOfCandles = 1;
	    if(!initializedDayCandles) {
		numberOfCandles = 200;
		initializedDayCandles = true;
	    }
	    List<BidAskCandle> candles = getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity.DAY, Instant.now(), numberOfCandles, true, true);
	    LOG.info("Done fetching one day candles, got {} of them.", candles.size());
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }
    
    
    /*
     * Call the oanda api to get candles.
     */
    private List<BidAskCandle> getStoreAndNotifyCandlesForAllInstruments(CandleStickGranularity granularity, Instant end, Integer number, boolean doSave, boolean doNotify) throws UnsupportedEncodingException {
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);
	final List<BidAskCandle> allCandles = new ArrayList<>();
	
	//Consumer used to handle one instrument
	Consumer<Instrument> ic = instrument -> {
	    try {
		Set<BidAskCandle> candles = getCandlesForOneInstrument(instrument, granularity, end, number);
		allCandles.addAll(candles);
		candles.forEach(bac -> {
		    if(doSave) {
			bidAskCandleRepo.save(bac);
		    }
		    if(doNotify) {
			candleEventBus.notify("candles." + instrument, Event.wrap(bac));
		    }
		    });
		LOG.debug("{} {} candles saved and sent to event bus for instrument {} ({})",candles.size(), granularity, instrument, candles.isEmpty() ? null : candles.iterator().next());
	    } catch (Exception e) {
		LOG.error("Error fetching {} candles",granularity, e);
	    }
	};
	
	//Attach consumers
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	
	Arrays.asList(Instrument.values()).forEach(i-> publisher.onNext(i));
	publisher.onComplete();
	//Sort them by time
	Collections.sort(allCandles, new Comparator<BidAskCandle>() {
	    @Override
	    public int compare(BidAskCandle o1, BidAskCandle o2) {
		if(o1 == null) {
		    return -1;
		}
		if(o2 == null) {
		    return 1;
		}
		return o1.time.compareTo(o2.time);
	    }});
	return allCandles;
    }


    private Set<BidAskCandle> getCandlesForOneInstrument(Instrument instrument, CandleStickGranularity granularity, Instant end, Integer number) throws UnsupportedEncodingException {
	OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, granularity, number, null, end);
	return bidAskCandles.getCandles().stream()
		.map(bac -> new BidAskCandle(instrument, Broker.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(),bac.getComplete()))
		.collect(Collectors.toSet());
    }

}
