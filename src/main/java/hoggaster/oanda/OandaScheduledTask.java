package hoggaster.oanda;

import com.codahale.metrics.annotation.Timed;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DepotMonitorImpl;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.dispatch.WorkQueueDispatcher;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.fn.tuple.Tuple;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

import static hoggaster.rules.indicators.candles.CandleStickGranularity.END_OF_DAY;
import static hoggaster.rules.indicators.candles.CandleStickGranularity.MINUTE;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * Right now some kind of collection of scheduled methods. Some scheduled methods also resides in the {@link DepotMonitorImpl}
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

    private boolean initComplete = false;

    private Set<OandaInstrument> instrumentsForMainAccount = new HashSet<OandaInstrument>();

    private int cpsFetchedRequired;
    private LongAdder cpsFetched = new LongAdder();

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
        Stream<Tuple2<CurrencyPair, CandleStickGranularity>> stream = Streams.wrap(publisher);

        // Attach  consumers
        stream.consumeOn(new WorkQueueDispatcher("FetchCandlesDispatcher",4,16, e->LOG.warn("Exception fetching historic candles",e)), t -> {
            candleService.fetchAndSaveHistoricCandles(t.getT1(), t.getT2());
            cpsFetched.increment();
            LOG.info("Cps fetched is now {}, required is {}", cpsFetched.intValue(), cpsFetchedRequired);
        });

        cpsFetchedRequired = CurrencyPair.MAJORS.length * 2;
        LOG.info("Need to fetch {} cp/candle types combos", cpsFetchedRequired);

        Environment.timer().submit(time -> {
            asList(CurrencyPair.MAJORS).forEach(currencyPair -> publisher.onNext(Tuple.of(currencyPair, MINUTE)));
            asList(CurrencyPair.MAJORS).forEach(currencyPair -> publisher.onNext(Tuple.of(currencyPair, END_OF_DAY)));
            publisher.onComplete();
        }, 10, SECONDS);
    }


    /**
     * Fetch all instruments available for the main account and update the list we use when fetching prices.
     */
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    void fetchInstruments() {
        try {
            LOG.info("Start fetching currencyPair definitions");
            Instruments availableInstruments = oanda.getInstrumentsForAccount(Integer.valueOf(oandaProps.getMainAccountId().trim()));
            // Only tradeOpened the ones we have support for
            availableInstruments.getInstruments().forEach(i -> {
                try {
                    CurrencyPair.valueOf(i.instrument); //throws if not found
                    instrumentsForMainAccount.add(i);
                    LOG.debug("Fetched a currencyPair: {}", i);
                } catch (IllegalArgumentException e) {
                    LOG.debug("Missing CurrencyPair enum for currencyPair {}", i.instrument);
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


    /*
     * Get the one minute candles wtf is this method doing in this class??
     */
    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 6000)
    @Timed
    public void fetchMinuteCandles() {
        if(cpsFetched.intValue() < cpsFetchedRequired){
            LOG.info("Skip fetching minute candles since warmup is not ready...");
            return;
        }
        LOG.info("About to fetch one minute candles");
        try {
            List<Candle> candles = fetchAndDispatchLastCandleForAllInstruments(MINUTE);
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
        if(cpsFetched.intValue() < cpsFetchedRequired){
            LOG.info("Skip fetching end_of_day candles since warmup is not ready...");
            return;
        }
        LOG.info("About to fetch one day candles");
        try {
            List<Candle> candles = fetchAndDispatchLastCandleForAllInstruments(END_OF_DAY);
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

        asList(CurrencyPair.values()).forEach(i -> publisher.onNext(i));
        publisher.onComplete();
        return allCandles;
    }
}
