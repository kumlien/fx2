package hoggaster.domain.brokers;

import com.codahale.metrics.annotation.Timed;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depot.Position;
import hoggaster.oanda.responses.*;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface BrokerConnection {

    @Timed
    List<Position> getPositions(String depotId);

    Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException;

    OandaAccounts getAccounts();

    // TODO remove Oanda x 2
    OandaPrices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;

    Broker getBrokerID();

    // TODO remove Oanda
    OandaBidAskCandlesResponse getBidAskCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, Integer periods, Instant start, Instant end, boolean includeFirst);

    BrokerDepot getDepot(String depotId);
}
