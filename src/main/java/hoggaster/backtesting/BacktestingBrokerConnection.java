package hoggaster.backtesting;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.depots.Position;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.responses.*;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Used for backtesting
 */
@Service("BacktestingBrokerConnection")
public class BacktestingBrokerConnection implements BrokerConnection, OrderService {


    @Override
    public OandaOrderResponse sendOrder(OrderRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Position> getPositions(String depotId) {
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
    public OandaPrices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
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
    public List<Trade> getOpenTrades(String fx2DepotId, String brokerDepotId) {
        return null;
    }

    @Override
    public Optional<Trade> getTrade(String depotId, String brokerId, String tradeId) {
        return null;
    }


}
