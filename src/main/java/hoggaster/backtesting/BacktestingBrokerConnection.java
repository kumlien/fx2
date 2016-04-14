package hoggaster.backtesting;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.positions.ClosePositionResponse;
import hoggaster.domain.positions.Position;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.responses.*;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;


/**
 * Used for backtesting
 */
@Service("BacktestingBrokerConnection")
public class BacktestingBrokerConnection implements BrokerConnection {


    @Override
    public OandaCreateTradeResponse sendOrder(OrderRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Position> getPositions(String depotId) {
        return null;
    }

    @Override
    public ClosePositionResponse closePosition(Integer accountId, CurrencyPair instrument) {
        return null;
    }

    @Override
    public Instruments getInstrumentsForAccount(Integer mainAccountId) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OandaAccounts getAccounts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OandaPrices getPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Observable<OandaPrices> getPricesAsync(Set<OandaInstrument> instruments) {
        return null;
    }

    @Override
    public Broker getBrokerID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OandaBidAskCandlesResponse getBidAskCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, Integer periods, Instant start, Instant end, boolean includeFirst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BrokerDepot getDepot(String depotId) {
        return null;
    }

    @Override
    public Set<Trade> getOpenTrades(String fx2DepotId, String brokerDepotId) {
        return null;
    }

    @Override
    public Optional<Trade> getTrade(String depotId, String brokerId, String tradeId) {
        return null;
    }

    @Override
    public CloseTradeResponse closeTrade(Trade trade, String brokerAccountId) {
        return null;
    }
}
