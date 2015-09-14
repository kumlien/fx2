package hoggaster.backtesting;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.domain.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.responses.Accounts;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.oanda.responses.OandaPrices;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Service;


/**
 * Used for backtesting
 * 
 */
@Service("BacktestingBrokerConnection")
public class BacktestingBrokerConnection implements BrokerConnection, OrderService {
    
    

    @Override
    public OandaOrderResponse sendOrder(OrderRequest request) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Instruments getInstrumentsForAccount(Integer mainAccountId) throws UnsupportedEncodingException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Accounts getAccounts() {
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
    public OandaBidAskCandlesResponse getBidAskCandles(Instrument instrument, CandleStickGranularity granularity, Integer periods, Instant start, Instant end, boolean includeFirst) {
	// TODO Auto-generated method stub
	return null;
    }

}
