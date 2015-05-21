package hoggaster.domain;

import hoggaster.BrokerID;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.responses.Accounts;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.oanda.responses.Prices;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Set;


public interface Broker {
	
	
	OandaOrderResponse sendOrderToBroker(OrderRequest request);

	Instruments getInstrumentsForAccount(Integer mainAccountId) throws UnsupportedEncodingException;

	Accounts getAccounts();

	Prices getAllPrices(Set<OandaInstrument> instrumentsForMainAccount) throws UnsupportedEncodingException;
	
	BrokerID getBrokerID();

	public abstract OandaBidAskCandlesResponse getBidAskCandles(Instrument instrument, CandleStickGranularity granularity,
			Integer periods, Instant start, Instant end)
			throws UnsupportedEncodingException;
}
