package hoggaster.oanda;

import com.codahale.metrics.annotation.Timed;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.depot.DepotMonitorImpl;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaPrices;
import hoggaster.prices.Price;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.fn.tuple.Tuple;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.*;



/**
 * Right now some kind of collection of scheduled methods. Some scheduled methods also resides in the {@link DepotMonitorImpl} TODO Fetches prices via pull, implement push/streaming from oanda instead.
 */
@Component
public class OandaScheduledTask {

    private static final Logger LOG = LoggerFactory.getLogger(OandaScheduledTask.class);

    static final long ONE_MINUTE = 60L * 1000;

    private final BrokerConnection oanda;

    private final EventBus priceEventBus;

    private final EventBus candleEventBus;

    private final OandaProperties oandaProps;

    private final CandleService candleService;

    private Set<OandaInstrument> instrumentsForMainAccount = new HashSet<OandaInstrument>();

    @Autowired
    public OandaScheduledTask(@Qualifier("OandaBrokerConnection") BrokerConnection oanda, @Qualifier("priceEventBus") EventBus priceEventBus, @Qualifier("candleEventBus") EventBus candleEventBus, OandaProperties oandaProps, CandleService candleService) {
        this.oanda = oanda;
        this.priceEventBus = priceEventBus;
        this.oandaProps = oandaProps;
        this.candleEventBus = candleEventBus;
        this.candleService = candleService;
    }


    /**
     * Fill the db with candles for the specified currencyPair and granularity.
     * Only needed at startup to get us up to date.
     */
    @PostConstruct
    void fetchAllHistoricData() {
        RingBufferWorkProcessor<Tuple2<CurrencyPair, CandleStickGranularity>> publisher = RingBufferWorkProcessor.create("Candle work processor", 256);
        Stream<Tuple2<CurrencyPair, CandleStickGranularity>> instrumentStream = Streams.wrap(publisher);

        // Consumer used to handle one currencyPair
        Consumer<Tuple2<CurrencyPair, CandleStickGranularity>> ic = t -> {
            try {
                candleService.fetchAndSaveHistoricCandles(t.getT1(), t.getT2());
            } catch (Exception e) {
                LOG.error("Error fetching {} candles", t, e);
            }
        };

        // Attach  consumers
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);

        Arrays.asList(CurrencyPair.MAJORS).forEach(i -> publisher.onNext(Tuple.of(i, CandleStickGranularity.MINUTE)));
        Arrays.asList(CurrencyPair.MAJORS).forEach(i -> publisher.onNext(Tuple.of(i, CandleStickGranularity.END_OF_DAY)));
        publisher.onComplete();
    }


    /**
     * Fetch all instruments available for the main account and update the list we use when fetching prices.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    void fetchInstruments() {
        try {
            LOG.info("Start fetching currencyPair definitions");
            Instruments availableInstruments = oanda.getInstrumentsForAccount(Integer.valueOf(oandaProps.getMainAccountId().trim()));
            // Only add the ones we have support for
            availableInstruments.getInstruments().forEach(i -> {
                try {
                    CurrencyPair.valueOf(i.instrument); //throws if not found
                    instrumentsForMainAccount.add(i);
                    LOG.info("Fetched an currencyPair: {}", i);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Missing CurrencyPair enum for currencyPair {}", i.instrument);
                    instrumentsForMainAccount.remove(i.instrument);
                }
            });
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to access oanda", e);
        }
    }


    public void logAccounts() {
        oanda.getAccounts().getAccounts().forEach(a -> {
            LOG.info("Account: {}", a);
        });
    }


    //@Scheduled(cron = "*/5 * * * * *")
    void fetchPrices() throws UnsupportedEncodingException {
        try {
            if (instrumentsForMainAccount == null) {
                fetchInstruments();
            }
            if (instrumentsForMainAccount == null || instrumentsForMainAccount.size() < 1) {
                LOG.warn("No instruments known yet, skip call to fetch prices...");
                return;
            }

            OandaPrices allPrices = oanda.getAllPrices(instrumentsForMainAccount);
            LOG.info("Got {} prices, send them to priceEventBus", allPrices.prices.size());
            allPrices.prices.forEach(p -> priceEventBus.notify("prices." + p.instrument, Event.wrap(new Price(p))));
        } catch (Exception e) {
            LOG.error("Unhandled error in scheduled fetchPrices method", e);
        }
    }


    /*
     * Get the one minute candles wtf is this method doing in this class??
     */
    //@Scheduled(fixedRate = ONE_MINUTE, initialDelay = 6000)
    @Timed
    public void fetchMinuteCandles() {
        LOG.info("About to fetch one minute candles");
        try {
            List<Candle> candles = fetchAndDispatchLastCandleForAllInstruments(CandleStickGranularity.MINUTE);
            LOG.info("Got {} one minute candles", candles.size());
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error publishing last minute candle", e);
        }
    }


    /*
     * Get the daily candles.
     * 
     * TODO this one needs to take into account the different opening/closing times for different instruments (and possibly in combination with different brokers) for now we hard code it to open at 17:01:00 and close at 16:59:59 New York time, that is, we record the day candle at this point.
     */
    @Scheduled(cron = "0 1 17 * * MON-FRI", zone = "America/New_York")
    @Timed
    public void fetchDayCandles() {
        LOG.info("About to fetch one day candles");
        try {
            List<Candle> candles = fetchAndDispatchLastCandleForAllInstruments(CandleStickGranularity.END_OF_DAY);
            if(candles != null && !candles.isEmpty()) {
                LOG.info("Done fetching one day candle, got {}", candles.get(0));
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error fetching candles", e);
        }
    }


    /*
     * Call the oanda api to get candles and put them on the eventbus.
     */
    private List<Candle> fetchAndDispatchLastCandleForAllInstruments(CandleStickGranularity granularity) throws UnsupportedEncodingException {
        RingBufferWorkProcessor<CurrencyPair> publisher = RingBufferWorkProcessor.create("Candle work processor", 32);
        Stream<CurrencyPair> instrumentStream = Streams.wrap(publisher);
        final List<Candle> allCandles = new ArrayList<>();

        // Consumer used to handle one currencyPair
        Consumer<CurrencyPair> ic = instrument -> {
            try {
                List<Candle> candles = candleService.fetchAndSaveLatestCandlesFromBroker(instrument, granularity, 1);
                allCandles.addAll(candles);
                candles.forEach(bac -> {
                    candleEventBus.notify("candles." + instrument, Event.wrap(bac));
                });
                LOG.debug("{} {} candles saved and sent to event bus for currencyPair {} ({})", candles.size(), granularity, instrument, candles.isEmpty() ? null : candles.get(0));
            } catch (Exception e) {
                LOG.error("Error fetching {} candles", granularity, e);
            }
        };

        // Attach consumers
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);

        Arrays.asList(CurrencyPair.values()).forEach(i -> publisher.onNext(i));
        publisher.onComplete();
        return allCandles;
    }
}
