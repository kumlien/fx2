package hoggaster.oanda;

import com.codahale.metrics.annotation.Timed;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
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
 * Right now some kind of collection of scheduled methods. Some scheduled methods also resides in the {@link hoggaster.user.depot.DepotMonitorImpl} TODO Fetches prices via pull, implement push/streaming from oanda instead.
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

    private static boolean historicDataFetched = false;

    @Autowired
    public OandaScheduledTask(@Qualifier("OandaBrokerConnection") BrokerConnection oanda, @Qualifier("priceEventBus") EventBus priceEventBus, @Qualifier("candleEventBus") EventBus candleEventBus, OandaProperties oandaProps, CandleService candleService) {
        this.oanda = oanda;
        this.priceEventBus = priceEventBus;
        this.oandaProps = oandaProps;
        this.candleEventBus = candleEventBus;
        this.candleService = candleService;
    }


    /**
     * Fill the db with candles for the specified instrument and granularity.
     * Only needed at startup to make us up to date.
     */
    //@PostConstruct
    void fetchAllHistoricData() {
        Arrays.asList(Instrument.MAJORS).forEach(i -> candleService.fetchAndSaveHistoricCandles(i, CandleStickGranularity.END_OF_DAY));
        Arrays.asList(Instrument.MAJORS).forEach(i -> candleService.fetchAndSaveHistoricCandles(i, CandleStickGranularity.MINUTE));
        historicDataFetched = true;
    }

    @PostConstruct
    void fetchAllHistoricData2() {
        RingBufferWorkProcessor<Tuple2<Instrument, CandleStickGranularity>> publisher = RingBufferWorkProcessor.create("Candle work processor", 256);
        Stream<Tuple2<Instrument, CandleStickGranularity>> instrumentStream = Streams.wrap(publisher);

        // Consumer used to handle one instrument
        Consumer<Tuple2<Instrument, CandleStickGranularity>> ic = t -> {
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

        Arrays.asList(Instrument.MAJORS).forEach(i -> publisher.onNext(Tuple.of(i, CandleStickGranularity.MINUTE)));
        Arrays.asList(Instrument.MAJORS).forEach(i -> publisher.onNext(Tuple.of(i, CandleStickGranularity.END_OF_DAY)));
        publisher.onComplete();
    }


    /**
     * Fetch all instruments available for the main account and update the list we use when fetching prices.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    void fetchInstruments() {
        try {
            LOG.info("Start fetching instrument definitions");
            Instruments availableInstruments = oanda.getInstrumentsForAccount(Integer.valueOf(oandaProps.getMainAccountId().trim()));
            // Only add the ones we have support for
            availableInstruments.getInstruments().forEach(i -> {
                try {
                    Instrument.valueOf(i.instrument); //throws if not found
                    instrumentsForMainAccount.add(i);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Missing Instrument enum for instrument {}", i.instrument);
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


    @Scheduled(cron = "*/5 * * * * *")
    void fetchPrices() throws UnsupportedEncodingException {
        if (!historicDataFetched) return;
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
    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 6000)
    @Timed
    public void fetchMinuteCandles() {
        if(!historicDataFetched) return;
        LOG.info("About to fetch one minute candles");
        try {
            List<Candle> candles = getAndNotifyCandlesForAllInstruments(CandleStickGranularity.MINUTE, 1);
            LOG.info("Got {} one minute candles", candles.size());
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error publishing last minutue candle", e);
        }
    }


    /*
     * Get the daily candles.
     * 
     * TODO this one needs to take into account the different opening/closing times for different instruments (and possibly in combination with different brokers) for now we hard code it to open at 17:00:00 and close at 16:59:59 New York time, that is, we record the day candle at this point.
     */
    @Scheduled(cron = "0 0 17 * * MON-FRI")
    @Timed
    public void fetchDayCandles() {
        if(!historicDataFetched) return;
        LOG.info("About to fetch one day candles");
        try {
            List<Candle> candles = getAndNotifyCandlesForAllInstruments(CandleStickGranularity.END_OF_DAY, 1);
            LOG.info("Done fetching one day candles, got {} of them.", candles.size());
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error fetching candles", e);
        }
    }


    /*
     * Call the oanda api to get candles.
     */
    private List<Candle> getAndNotifyCandlesForAllInstruments(CandleStickGranularity granularity, Integer number) throws UnsupportedEncodingException {
        RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("Candle work processor", 32);
        Stream<Instrument> instrumentStream = Streams.wrap(publisher);
        final List<Candle> allCandles = new ArrayList<>();

        // Consumer used to handle one instrument
        Consumer<Instrument> ic = instrument -> {
            try {
                List<Candle> candles = candleService.fetchAndSaveLatestCandlesFromBroker(instrument, granularity, number);
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
