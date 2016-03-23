package hoggaster.domain.brokers;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.positions.ClosePositionResponse;
import hoggaster.domain.positions.Position;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.responses.*;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface BrokerConnection extends OrderService {

    Set<Position> getPositions(String depotId);

    ClosePositionResponse closePosition(Integer accountId, CurrencyPair instrument);

    Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException;

    OandaAccounts getAccounts();

    // TODO remove Oanda x 2
    OandaPrices getPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;

    Broker getBrokerID();

    // TODO remove Oanda
    OandaBidAskCandlesResponse getBidAskCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, Integer periods, Instant start, Instant end,
            boolean includeFirst);

    BrokerDepot getDepot(String depotId);

    /**
     * Get all open trades for a depot from the broker
     *
     * @param fx2DepotId
     * @param brokerDepotId
     * @return List of Trade :s
     */
    Set<Trade> getOpenTrades(String fx2DepotId, String brokerDepotId);

    /**
     * Get a specific trade by id.
     *
     * @param depotId
     * @param brokerId
     * @param tradeId
     * @return The trade if found
     */
    Optional<Trade> getTrade(String depotId, String brokerId, String tradeId);

    CloseTradeResponse closeTrade(Trade trade, String brokerAccountId);
}
