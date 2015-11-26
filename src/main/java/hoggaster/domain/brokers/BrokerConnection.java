package hoggaster.domain.brokers;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.Position;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.responses.*;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface BrokerConnection {

    List<Position> getPositions(String depotId);

    Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException;

    OandaAccounts getAccounts();

    // TODO remove Oanda x 2
    OandaPrices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;

    Broker getBrokerID();

    // TODO remove Oanda
    OandaBidAskCandlesResponse getBidAskCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, Integer periods, Instant start, Instant end, boolean includeFirst);

    BrokerDepot getDepot(String depotId);

    /**
     * Get all open trades for a depot from the broker
     *
     * @param fx2DepotId
     * @param brokerDepotId
     * @return List of Trade :s
     */
    List<Trade> getOpenTrades(String fx2DepotId, String brokerDepotId);
}
