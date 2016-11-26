package hoggaster.candles;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class BidAskCandleServiceImpl implements CandleService {

    private static final Logger LOG = LoggerFactory.getLogger(BidAskCandleServiceImpl.class);

    //The earliest date we try to fetch candles from
    private static final Instant FIRST_CANDLE_DATE = Instant.now().truncatedTo(SECONDS).minus(Duration.ofDays(365));

    private final CandleRepo repo;

    private final BrokerConnection brokerConnection;

    @Autowired
    public BidAskCandleServiceImpl(CandleRepo repo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.repo = repo;
        this.brokerConnection = brokerConnection;
    }

    @Override
    public List<Candle> getLatestCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, int numberOfCandles) {
        Preconditions.checkArgument(numberOfCandles > 0 && numberOfCandles < 5000);
        LOG.info("Will try to get candles for {} for granularity {} with {} data points", currencyPair, granularity, numberOfCandles);
        Pageable pageable = new PageRequest(0, numberOfCandles);

        List<Candle> candles = repo.findByCurrencyPairAndGranularityOrderByTimeDesc(currencyPair, granularity, pageable);
        LOG.info("Got a list of {} {} candles with {} elements",currencyPair, granularity, candles.size());
        if (candles.size() < numberOfCandles) {
            LOG.info("Not all candles found in db ({} out of {}), will try to fetch the rest from oanda.", candles.size(), numberOfCandles);
            List<Candle> fetchedCandles = new ArrayList<>();
            fetchedCandles.addAll(getFromBroker(currencyPair, granularity, null, Instant.now(), numberOfCandles));
            LOG.info("Fetched {} additional candles from broker", fetchedCandles.size());
            candles.addAll(fetchedCandles);
        }
        return candles;
    }

    @Override
    public Candle fetchAndSaveLastCompleteCandle(CurrencyPair currencyPair, CandleStickGranularity granularity) {
        LOG.info("Fetch the {} latest candles for {}:{} ", 2, currencyPair, granularity);
        List<Candle> candles = getFromBroker(currencyPair, granularity, null, Instant.now(),2); //+1 to include any current incomplete candle
        LOG.info("Got {} candles back", candles.size());
        List<Candle> completeCandles = new ArrayList<>();
        if (!candles.isEmpty()) {
            completeCandles = candles.stream().filter(c -> c.complete).collect(Collectors.toList());
            repo.save(completeCandles);
            LOG.info("{} complete candles saved to db", completeCandles.size());
        }
        return completeCandles.size() > 0 ? completeCandles.get(0) : null;
    }


    /*
     * Used to make sure an currencyPair/granularity combo is up to date in the db.
     *
     * TODO we should also delete the first candle from our db if it's not complete!
     */
    @Override
    @Timed
    public int fetchAndSaveHistoricCandles(CurrencyPair currencyPair, CandleStickGranularity granularity) {
        Instant startDate = FIRST_CANDLE_DATE;
        List<Candle> existingCandles = repo.findByCurrencyPairAndGranularityOrderByTimeDesc(currencyPair, granularity, new PageRequest(0, 1));
        if(existingCandles != null && !existingCandles.isEmpty()) {
            startDate = existingCandles.get(0).time;
            LOG.info("Found a saved entry for {}: {}, will use that start date ({})", currencyPair, granularity, startDate);
        }
        LOG.info("Start fetching historic candles for {} ({}) starting at {}", currencyPair, granularity, startDate);
        int totalFetched = 0;
        List<Candle> candles = getFromBroker(currencyPair, granularity, startDate, null, 5000, true);
        Candle lastCandle = null;
        //Do loop until we don't get any more candle or until the last one is not completed.
        while (!candles.isEmpty()) { //Need to check for empty list (if we are starting up at a weekend)
            LOG.info("Received {} candles", candles.size());
            totalFetched += candles.size();
            lastCandle = candles.get(candles.size() - 1);
            repo.save(candles);
            LOG.info("Last candle received: {}", lastCandle);
            if(!lastCandle.complete) {
                repo.delete(lastCandle);
                break;
            }
            candles = getFromBroker(currencyPair, granularity, lastCandle.time, null, 4999, false);
        }

        LOG.info("Received an empty list of candles or an incomplete last candle, assume we are done after {} candles, last candle received: {}", totalFetched, lastCandle);
        return totalFetched;
    }


    private List<Candle> getFromBroker(CurrencyPair currencyPair, CandleStickGranularity granularity, Instant startDate, Instant endDate, int number, boolean includeFirst) {
        LOG.info("Will try to fetch {} candles with startDate {} and endDate {}", number, startDate, endDate);
        OandaBidAskCandlesResponse bidAskCandles = brokerConnection.getBidAskCandles(currencyPair, granularity, number, startDate, endDate, includeFirst);
        return bidAskCandles.getCandles().stream()
                .map(bac -> new Candle(currencyPair, Broker.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete())).collect(Collectors.toList());
    }


    //Call the real one with includeFirst = false
    private List<Candle> getFromBroker(CurrencyPair currencyPair, CandleStickGranularity granularity, Instant startDate, Instant endDate, int number) {
        return getFromBroker(currencyPair, granularity, startDate, endDate, number, false);
    }
}
