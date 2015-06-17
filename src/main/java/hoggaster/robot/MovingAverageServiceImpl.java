package hoggaster.robot;

import hoggaster.candles.BidAskCandle;
import hoggaster.candles.BidAskCandleRepo;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import com.google.common.collect.Sets;


@Service
public class MovingAverageServiceImpl implements MovingAverageService {
    
    static final long ONE_MINUTE = 60L * 1000;
    
    static final long ONE_HOUR = ONE_MINUTE * 60;
    
    static final long ONE_DAY = ONE_HOUR * 24;

    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageServiceImpl.class);

    private final BidAskCandleRepo bidAskCandleRepo;

    private final Broker oanda;

    // We need some kind of cache/storage here...

    @Autowired
    public MovingAverageServiceImpl(BidAskCandleRepo bidAskCandleRepo, Broker oanda) {
	this.bidAskCandleRepo = bidAskCandleRepo;
	this.oanda = oanda;
    }
    
    
    @Override
    public Double getMovingAverage(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints) {
	LOG.info("Will try to calculate moving average for {} for granularity {} with {} data points", instrument, granularity, numberOfDataPoints);
	return 0.0;
    }

    /*
     * Get the one minute candles
     */
    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 5000)
    void getMinuteCandles() {
	try {
	    Instant start = Instant.now();
	    Set<BidAskCandle> candles = getCandlesForAllInstruments(CandleStickGranularity.MINUTE, null, start, 1);
	    Duration duration = Duration.between(start, Instant.now());
	    LOG.info("Got {} one minute candles and the duration was {}", candles.size(), duration );
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }
    
    /*
     * Get the daily candles
     */
    @Scheduled(fixedRate = ONE_DAY, initialDelay = 50000)
    void getDayCandles() {
	try {
	    Set<BidAskCandle> candles = getCandlesForAllInstruments(CandleStickGranularity.DAY, null, Instant.now(), 1);
	    LOG.info("Got one day candles: {}", candles );
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }
    
    
    
    /*
     * Call the oanda api to get candles.
     */
    private Set<BidAskCandle> getCandlesForAllInstruments(CandleStickGranularity granularity, Instant start, Instant end, Integer number) throws UnsupportedEncodingException {
	Instant now = Instant.now();
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);
	final Set<BidAskCandle> allCandles = Sets.newHashSet();
	
	Consumer<Instrument> ic = instrument -> {
	    try {
		Set<BidAskCandle> candles = getCandlesForOneInstrument(instrument, granularity, null, now, number);
		allCandles.addAll(candles);
		candles.forEach(bidAskCandleRepo::save);
		LOG.info("{} {} candles saved for instrument {} ({})",candles.size(), granularity, instrument, candles.isEmpty() ? null : candles.iterator().next());
	    } catch (Exception e) {
		LOG.error("Error fetching {} candles",granularity, e);
	    }
	};
	
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	
	Arrays.asList(Instrument.values()).forEach(i-> publisher.onNext(i));
	publisher.onComplete();
	
	return allCandles;
    }


    private Set<BidAskCandle> getCandlesForOneInstrument(Instrument instrument, CandleStickGranularity granularity, Instant start, Instant end, Integer number) throws UnsupportedEncodingException {
	OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, granularity, number, start, end);
	return bidAskCandles.getCandles().stream()
		.map(bac -> new BidAskCandle(instrument, BrokerID.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(),bac.getComplete()))
		.collect(Collectors.toSet());
    }

}
