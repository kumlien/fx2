package hoggaster.candles;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

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

    @Override
    public List<Candle> getLatestCandles(Instrument instrument, CandleStickGranularity granularity, int numberOfCandles) {
	Preconditions.checkArgument(numberOfCandles > 0 && numberOfCandles < 5000);
	LOG.info("Will try to get candles for {} for granularity {} with {} data points", instrument, granularity, numberOfCandles);
	Pageable pageable = new PageRequest(0, numberOfCandles);

	// men hallå!!! hur fan tänkte du här?? du måste ju ha med ett datum i villkoret annars får du ju alltid träff (efter först inserten...)
	List<Candle> candles = repo.findByInstrumentAndGranularityOrderByTimeAsc(instrument, granularity, pageable);
	LOG.info("Got a list: {}", candles.size());
	if (candles.size() < numberOfCandles) {
	    LOG.info("Not all candles found in db ({} out of {}), will try to fetch the rest from oanda.", candles.size(), numberOfCandles);
	    List<Candle> fetchedCandles = new ArrayList<>();
	    fetchedCandles.addAll(getFromBroker(instrument, granularity, null, Instant.now(), numberOfCandles));
	    LOG.info("Fetched {} candles from broker", fetchedCandles.size());
	    candles.addAll(fetchedCandles);
	}
	return candles;
    }

    @Override
    public List<Candle> fetchAndSaveLatestCandles(Instrument instrument, CandleStickGranularity granularity, Integer number) {
	LOG.info("Fetch and save {} new candles for {}:{} ", number, instrument, granularity);
	List<Candle> candles = getFromBroker(instrument, granularity, null, Instant.now(), number);
	LOG.info("Got {} candles back", candles.size());
	if (!candles.isEmpty()) {
	    repo.save(candles);
	    LOG.info("{} candles saved to db", candles.size());
	}
	return candles;
    }

    @Override
    public int fetchAndSaveHistoricCandles(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate) {
	LOG.info("Start fetching historic candles for {} ({}) starting at {} ending at {}", instrument, granularity, startDate, endDate);
	int totalFetched = 0;
	Candle lastCandle = null;
	List<Candle> candles = getFromBroker(instrument, granularity, startDate, null, 5000);
	while (!candles.isEmpty()) {
	    LOG.info("Received {} candles", candles.size());
	    totalFetched += candles.size();
	    lastCandle = candles.get(candles.size() -1);
	    repo.save(candles);
	    LOG.info("Last candle received is from {}", lastCandle.time);
	    candles = getFromBroker(instrument, granularity, lastCandle.time, null, 5000, false);
	}
	
	LOG.info("Received an empty list of candles, assume we are done after {} candles, last candle received: {}", totalFetched, lastCandle);
	return totalFetched;
    }

    
    private List<Candle> getFromBroker(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate, int number, boolean includeFirst) {
	LOG.info("Will try to fetch {} candles with startDate {} and endDate {}", number, startDate, endDate);
	OandaBidAskCandlesResponse bidAskCandles = brokerConnection.getBidAskCandles(instrument, granularity, number, startDate, endDate, includeFirst);
	return bidAskCandles.getCandles().stream()
		.map(bac -> new Candle(instrument, Broker.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete())).collect(Collectors.toList());
    }
    
    
    //Call the real one with includeFirst = false
    private List<Candle> getFromBroker(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate, int number) {
	return getFromBroker(instrument, granularity, startDate, endDate, 5000, false);
    }
}
