package hoggaster.oanda;

import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.Prices;
import hoggaster.prices.Price;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import reactor.bus.Event;
import reactor.bus.EventBus;

//TODO Split
@Component
public class OandaScheduledTask {

    private static final Logger LOG = LoggerFactory.getLogger(OandaScheduledTask.class);

    private final Broker oanda;

    private final EventBus priceEventBus;

    private final OandaProperties oandaProps;



    private final String fetchPricesRegex; // Dammit, how to use NON constant
					   // values when setting up the
					   // scheduled tasks

    private final String fetchCandlesRegex;

    private Set<OandaInstrument> instrumentsForMainAccount = new HashSet<OandaInstrument>();

    @Autowired
    public OandaScheduledTask(@Qualifier("oandaApi") Broker oanda, @Qualifier("priceEventBus") EventBus priceReactor, OandaProperties oandaProps) {
	this.oanda = oanda;
	this.priceEventBus = priceReactor;
	this.oandaProps = oandaProps;
	this.fetchPricesRegex = oandaProps.getFetchPricesRegex();
	this.fetchCandlesRegex = oandaProps.getFetchCandlesRegex();
    }


    /**
     * Fetch all instruments available for the main account.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    public void fetchInstruments() {
	try {
	    Instruments availableInstruments = oanda.getInstrumentsForAccount(oandaProps.getMainAccountId());
	    // Only add the ones we have support for
	    availableInstruments.getInstruments().forEach(i -> {
		if (Instrument.valueOf(i.instrument) != null) {
		    instrumentsForMainAccount.add(i);
		} else {
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

    // @Scheduled(cron = "*/2 * * * * *")
    public void fetchPrices() throws UnsupportedEncodingException {
	try {
	    if (instrumentsForMainAccount == null) {
		fetchInstruments();
	    }
	    if (instrumentsForMainAccount == null || instrumentsForMainAccount.size() < 1) {
		LOG.warn("No instrument known yet, skip call to fetch prices...");
		return;
	    }

	    Prices allPrices = oanda.getAllPrices(instrumentsForMainAccount);
	    LOG.info("Got {} prices, send them to priceEventBus", allPrices.prices.size());
	    allPrices.prices.parallelStream().forEach(p -> priceEventBus.notify("prices." + p.instrument, Event.wrap(new Price(p))));
	} catch (Exception e) {
	    LOG.error("Unhandled error in scheduled fetchPrices method", e);
	}
    }
}
