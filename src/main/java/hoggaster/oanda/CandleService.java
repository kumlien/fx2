package hoggaster.oanda;

import hoggaster.candles.BidAskCandle;
import hoggaster.candles.BidAskCandleRepo;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.robot.MovingAverageService;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.time.Instant;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;

@Component
public class CandleService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CandleService.class);
    
    private final BidAskCandleRepo bidAskCandleRepo;
    
    private final Broker oanda;
    
    private final MovingAverageService maService;
    
    @Autowired
    public CandleService(BidAskCandleRepo bidAskCandleRepo, Broker oanda, MovingAverageService maService) {
	this.bidAskCandleRepo = bidAskCandleRepo;
	this.oanda = oanda;
	this.maService = maService;
    }
    
    
    /**
     * Make sure we have the last 200 candles for all instruments and
     * granularities we use. Not good to have this one in a postconstruct
     */

    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    void init() {
	Instant now = Instant.now();
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle init work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);

	Consumer<Instrument> ic = instrument -> {
	    Arrays.asList(CandleStickGranularity.values()).forEach(granularity -> {
		try {
		    OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, granularity, 200, null, now);
		    bidAskCandles.getCandles().forEach(bac -> {
			BidAskCandle candle = new BidAskCandle(instrument, BrokerID.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete());
			candle = bidAskCandleRepo.save(candle);
		    });
		    LOG.info("{} candles saved for instrument {}", bidAskCandles.getCandles().size(), instrument);
		} catch (Exception e) {
		    LOG.error("Error fetching candles", e);
		}
	    });
	};

	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);

	Arrays.asList(Instrument.values()).forEach(i -> publisher.onNext(i));
	publisher.onComplete();
    }
    
    
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    void getMinuteCandles() {
	Instant now = Instant.now();
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle minute work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);

	Consumer<Instrument> ic = instrument -> {
		try {
		    OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, CandleStickGranularity.MINUTE, 1, null, now);
		    bidAskCandles.getCandles().forEach(bac -> {
			BidAskCandle candle = new BidAskCandle(instrument, BrokerID.OANDA, CandleStickGranularity.MINUTE, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete());
			candle = bidAskCandleRepo.save(candle);
		    });
		    LOG.info("{} candles saved for instrument {}", bidAskCandles.getCandles().size(), instrument);
		} catch (Exception e) {
		    LOG.error("Error fetching candles", e);
		}
	};

	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);
	instrumentStream.consume(ic);

	Arrays.asList(Instrument.values()).forEach(i -> publisher.onNext(i));
	publisher.onComplete();
    }




}
