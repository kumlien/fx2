package hoggaster.candles;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service
public class BidAskCandleServiceImpl implements CandleService {

    private static final Logger LOG = LoggerFactory.getLogger(BidAskCandleServiceImpl.class);

    private final CandleRepo repo;

    private final BrokerConnection brokerConnection;

    @Autowired
    public BidAskCandleServiceImpl(CandleRepo repo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
	this.repo = repo;
	this.brokerConnection = brokerConnection;
    }

    
    //TODO Very inefficient. Need to figure out which candles we need to fetch from the broker.
    @Override
    public List<Candle> getCandles(Instrument instrument, CandleStickGranularity granularity, int numberOfCandles) {
	Preconditions.checkArgument(numberOfCandles > 0 && numberOfCandles < 5000);
	LOG.info("Will try to get candles for {} for granularity {} with {} data points", instrument, granularity, numberOfCandles);
	Pageable pageable = new PageRequest(0, numberOfCandles);
	List<Candle> candles = repo.findByInstrumentAndGranularityOrderByTimeDesc(instrument, granularity, pageable);
	LOG.info("Got a list: {}", candles.size());
	if (candles.size() < numberOfCandles) {
	    LOG.info("Not all candles found in db ({} out of {}), will try to fetch the rest from oanda.", candles.size(), numberOfCandles);
	    List<Candle> fetchedCandles = new ArrayList<>();
	    try {
		fetchedCandles.addAll(getFromBroker(instrument, granularity, Instant.now(), numberOfCandles));
		fetchedCandles.stream().forEach(repo::save);
	    } catch (UnsupportedEncodingException e) {
		LOG.error("Error fetching candles", e);
	    }
	    LOG.info("Fetched {} candles from broker", fetchedCandles.size());
	    candles.addAll(fetchedCandles);
	}
	return candles;
    }

    private List<Candle> getFromBroker(Instrument instrument, CandleStickGranularity granularity, Instant now, int number) throws UnsupportedEncodingException {
	OandaBidAskCandlesResponse bidAskCandles = brokerConnection.getBidAskCandles(instrument, granularity, number, null, now);
	return bidAskCandles.getCandles().stream()
		.map(bac -> new Candle(instrument, Broker.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete()))
		.collect(Collectors.toList());
    }
}
