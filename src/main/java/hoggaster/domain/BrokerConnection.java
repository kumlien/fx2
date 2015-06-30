package hoggaster.domain;

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


public interface BrokerConnection {
	
	
	OandaOrderResponse sendOrder(OrderRequest request);

	Instruments getInstrumentsForAccount(Integer mainAccountId) throws UnsupportedEncodingException;

	Accounts getAccounts();

	OandaPrices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;
	
	Broker getBrokerID();

	public abstract OandaBidAskCandlesResponse getBidAskCandles(Instrument instrument, CandleStickGranularity granularity,
			Integer periods, Instant start, Instant end)
			throws UnsupportedEncodingException;
}
