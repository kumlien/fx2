package hoggaster.robot;

import hoggaster.candles.BidAskCandle;
import hoggaster.candles.BidAskCandleRepo;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.time.Instant;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;

@Service
public class MovingAverageServiceImpl implements MovingAverageService {


    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageServiceImpl.class);

    private final BidAskCandleRepo bidAskCandleRepo;

    private final Broker oanda;

    // We need some kind of cache/storage here...

    public MovingAverageServiceImpl(BidAskCandleRepo bidAskCandleRepo, Broker oanda) {
	this.bidAskCandleRepo = bidAskCandleRepo;
	this.oanda = oanda;
    }
    
    
    @Override
    public Double getMovingAverage(Instrument instrument, CandleStickGranularity granularity, Integer numberOfDataPoints) {
	LOG.info("Will try to calculate moving average for {} for granularity {} with {} data points", instrument, granularity, numberOfDataPoints);
	return 0.0;
    }

    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    void getMinuteCandles() {
	Instant now = Instant.now();
	RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle minute work processor", 32);
	Stream<Instrument> instrumentStream = Streams.wrap(publisher);

	Consumer<Instrument> ic = instrument -> {
	    try {
		OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, CandleStickGranularity.MINUTE, 1, null, now);
		bidAskCandles.getCandles().forEach(
			bac -> {
			    BidAskCandle candle = new BidAskCandle(instrument, BrokerID.OANDA, CandleStickGranularity.MINUTE, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(),
				    bac.getComplete());
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
