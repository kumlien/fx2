package hoggaster.oanda;

import hoggaster.BrokerID;
import hoggaster.domain.Broker;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.requests.OandaOrderRequest;
import hoggaster.oanda.responses.Accounts;
import hoggaster.oanda.responses.InstrumentHistory;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaAccount;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.oanda.responses.Prices;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Set;

import javax.sound.midi.Instrument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codahale.metrics.annotation.Timed;

/**
 * Access point to oanda
 */
public class OandaApi implements Broker {
	
	private static final Logger LOG = LoggerFactory.getLogger(OandaApi.class);
	
	private final OandaResourcesProperties resources;
	
	private final RestTemplate restTemplate;
	
	private final HttpEntity<String> defaultStringEntity;
	
	private final HttpHeaders defaultHeaders;
	
	@Autowired
	public OandaApi(OandaProperties oandaProps, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
		this.restTemplate = restTemplate;
		this.resources = resources;
		defaultHeaders = new HttpHeaders();
		defaultHeaders.set("Accept-Encoding", "gzip, deflate");
		defaultHeaders.set("Connection", "Keep-Alive");
		defaultHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
		defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	defaultStringEntity = new HttpEntity<String>(defaultHeaders);
	}

	/**
	 * Get all {@link OandaAccount}s connected to the configured api-key
	 */
	@Timed
	public Accounts getAccounts() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getAccounts());
		String uri = builder.buildAndExpand("").toUriString();
		ResponseEntity<Accounts> accounts = restTemplate.exchange(uri, HttpMethod.GET, defaultStringEntity, Accounts.class);
		LOG.info("Found {} accounts", accounts.getBody().getAccounts().size());
		accounts.getBody().getAccounts().forEach(a -> LOG.info("Account: {}", a));
		return accounts.getBody();
	}
	
	/**
	 * Get all available {@link Instrument}s for the first account we find.
	 * @throws UnsupportedEncodingException 
	 */
	@Timed
	public Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getInstruments());
		URI uri = builder
				.queryParam("accountId", accountId)
				.queryParam("fields","displayName%2Cinstrument%2Cpip%2CmaxTradeUnits%2Cprecision%2CmaxTrailingStop%2CminTrailingStop%2CmarginRate%2Chalted%2CinterestRate")
				.build(true)
				.toUri();
		LOG.debug("uri: {}", uri);
		ResponseEntity<Instruments> instruments = restTemplate.exchange(uri, HttpMethod.GET, defaultStringEntity, Instruments.class);
		LOG.debug("Got {}", instruments.getBody().getInstruments());
		return instruments.getBody();
	}
	

	/**
	 * Fetch candles for all known instruments.
	 */
	@Timed
	public InstrumentHistory getCandles(Integer accountId) throws UnsupportedEncodingException {
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getCandles());
		String uri = builder.queryParam("instrument", getInstrumentsForAccount(accountId).getInstruments().get(0).instrument).toUriString();
		ResponseEntity<InstrumentHistory> candles = restTemplate.exchange(uri, HttpMethod.GET, defaultStringEntity, InstrumentHistory.class);
		LOG.info(candles.getBody() + "");
		return candles.getBody();
	}
	
	@Timed
	public Prices getAllPrices(Set<OandaInstrument> instruments) throws UnsupportedEncodingException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getPrices());
		StringBuilder sb = new StringBuilder();
		instruments.forEach(instrument -> sb.append(instrument.instrument).append("%2C"));
		URI uri = builder.queryParam("instruments", sb.toString()).build(true).toUri();
		ResponseEntity<Prices> prices = restTemplate.exchange(uri, HttpMethod.GET, defaultStringEntity, Prices.class);
		LOG.debug("Got {}", prices.getBody().prices);
		return prices.getBody();
	}
	

	
	@Timed
	public OandaOrderResponse sendOrderToBroker(OrderRequest request) {
		LOG.info("Sendning order to oanda: {}", request);
		MultiValueMap<String, String> oandaRequest = new OandaOrderRequest(request.instrument, request.units, request.side, request.type, request.expiry, request.price);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getOrders());
//		builder.queryParam("instrument", request.instrument)
//			.queryParam("units", request.units)
//			.queryParam("side", request.side)
//			.queryParam("type", request.type);
//		if(null != request.expiry) {
//			builder.queryParam("expiry", request.expiry);
//		}
//		if(null != request.price) {
//			builder.queryParam("price", request.price).toUriString();
//		}
		String uri = builder.buildAndExpand(request.externalDepotId).toUriString();
		LOG.info("Sending order to oanda: {}", uri);

		HttpEntity<MultiValueMap> httpEntity = new HttpEntity<MultiValueMap>(oandaRequest, defaultHeaders);
		ResponseEntity<OandaOrderResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, OandaOrderResponse.class);
		return responseEntity.getBody();
	}

	@Override
	public BrokerID getBrokerID() {
		return BrokerID.OANDA;
	}
}
