package hoggaster.domain;

import hoggaster.oanda.responses.Accounts;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaPrices;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Set;

public interface BrokerConnection {

    Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException;

    Accounts getAccounts();

    // TODO remove Oanda x 2
    OandaPrices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;

    Broker getBrokerID();

    // TODO remove Oanda
    OandaBidAskCandlesResponse getBidAskCandles(Instrument instrument, CandleStickGranularity granularity, Integer periods, Instant start, Instant end, boolean includeFirst);
}
