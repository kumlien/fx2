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

    
    @Override
    public List<Candle> getLatestCandles(Instrument instrument, CandleStickGranularity granularity, int numberOfCandles) {
	Preconditions.checkArgument(numberOfCandles > 0 && numberOfCandles < 5000);
	LOG.info("Will try to get candles for {} for granularity {} with {} data points", instrument, granularity, numberOfCandles);
	Pageable pageable = new PageRequest(0, numberOfCandles);
	
	//men hallå!!! hur fan tänkte du här?? du måste ju ha med ett datum i villkoret annars får du ju alltid träff (efter först inserten...)
	List<Candle> candles = repo.findByInstrumentAndGranularityOrderByTimeAsc(instrument, granularity, pageable);
	LOG.info("Got a list: {}", candles.size());
	if (candles.size() < numberOfCandles) {
	    LOG.info("Not all candles found in db ({} out of {}), will try to fetch the rest from oanda.", candles.size(), numberOfCandles);
	    List<Candle> fetchedCandles = new ArrayList<>();
	    try {
		fetchedCandles.addAll(getFromBroker(instrument, granularity, null, Instant.now(), numberOfCandles));
		
	    } catch (UnsupportedEncodingException e) {
		LOG.error("Error fetching candles", e);
	    }
	    LOG.info("Fetched {} candles from broker", fetchedCandles.size());
	    candles.addAll(fetchedCandles);
	}
	return candles;
    }

    private List<Candle> getFromBroker(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate, int number) throws UnsupportedEncodingException {
	LOG.info("Will try to fetch {} candles with startDate {} and endDate {}", number, startDate, endDate);
	OandaBidAskCandlesResponse bidAskCandles = brokerConnection.getBidAskCandles(instrument, granularity, number, startDate, endDate);
	return bidAskCandles.getCandles().stream()
		.map(bac -> new Candle(instrument, Broker.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete()))
		.collect(Collectors.toList());
    }


    @Override
    public List<Candle> fetchAndSaveNewCandles(Instrument instrument, CandleStickGranularity granularity, Integer number) {
	
	try {
	    List<Candle> candles = getFromBroker(instrument, granularity, null, Instant.now(), number);
	    repo.save(candles);
	    return candles;
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException("Error fetching candles from broker.", e);
	}
    }


    @Override
    public int fetchAndSaveHistoricCandles(Instrument instrument, CandleStickGranularity granularity, Instant startDate, Instant endDate) {
	// TODO Auto-generated method stub
	return 0;
    }
}
