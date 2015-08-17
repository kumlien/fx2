package hoggaster.oanda;

import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.requests.OandaOrderRequest;
import hoggaster.oanda.responses.Accounts;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaAccount;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.oanda.responses.OandaPrices;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codahale.metrics.annotation.Timed;

/**
 * Access point to oanda
 */
public class OandaApi implements BrokerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(OandaApi.class);

    private final OandaResourcesProperties resources;

    private final RestTemplate restTemplate;

    private final HttpEntity<String> defaultHttpEntity;

    private final HttpHeaders defaultHeaders;

    @Autowired
    public OandaApi(OandaProperties oandaProps, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
	this.restTemplate = restTemplate;
	this.resources = resources;
	defaultHeaders = new HttpHeaders();
	defaultHeaders.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
	defaultHeaders.set(HttpHeaders.CONNECTION, "Keep-Alive");
	defaultHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
	defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	defaultHttpEntity = new HttpEntity<String>(defaultHeaders);
    }

    /**
     * Get all {@link OandaAccount}s connected to the configured api-key
     */
    @Override
    @Timed
    public Accounts getAccounts() {
	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getAccounts());
	String uri = builder.buildAndExpand("").toUriString();
	ResponseEntity<Accounts> accounts = restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, Accounts.class);
	LOG.info("Found {} accounts", accounts.getBody().getAccounts().size());
	accounts.getBody().getAccounts().forEach(a -> LOG.info("Account: {}", a));
	return accounts.getBody();
    }

    /**
     * Get all available {@link Instrument}s for the first account we find.
     * 
     * @throws UnsupportedEncodingException
     */
    @Override
    @Timed
    public Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException {
	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getInstruments());
	URI uri = builder.queryParam("accountId", accountId).queryParam("fields", "displayName%2Cinstrument%2Cpip%2CmaxTradeUnits%2Cprecision%2CmaxTrailingStop%2CminTrailingStop%2CmarginRate%2Chalted%2CinterestRate").build(true).toUri();
	LOG.debug("uri: {}", uri);
	ResponseEntity<Instruments> instruments = restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, Instruments.class);
	LOG.debug("Got {}", instruments.getBody().getInstruments());
	return instruments.getBody();
    }

    /**
     * Fetch candles for the specified instrument.
     * 
     * count: Optional The number of candles to return in the response. This
     * parameter may be ignored by the server depending on the time range
     * provided. If not specified, count will default to 500. The maximum
     * acceptable value for count is 5000. count should not be specified if both
     * the start and end parameters are also specified. start2: Optional The
     * start timestamp for the range of candles requested. The value specified
     * must be in a valid datetime format. end2: Optional The end timestamp for
     * the range of candles requested. The value specified must be in a valid
     * datetime format.
     */
    @Override
    @Timed
    public OandaBidAskCandlesResponse getBidAskCandles(Instrument instrument, CandleStickGranularity granularity, Integer periods, Instant start, Instant end) throws UnsupportedEncodingException {

	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getCandles()).queryParam("instrument", instrument).queryParam("granularity", granularity.oandaStyle);

	if (start != null) {
	    String encoded = URLEncoder.encode(start.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
	    builder.queryParam("start", encoded);
	}
	if (end != null) {
	    String encoded = URLEncoder.encode(end.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
	    builder.queryParam("end", encoded);
	}
	if (periods != null) {
	    builder.queryParam("count", periods);
	}
	//String uri = builder.build(true).toUriString();

	ResponseEntity<OandaBidAskCandlesResponse> candles = restTemplate.exchange(builder.build(true).toUri(), HttpMethod.GET, defaultHttpEntity, OandaBidAskCandlesResponse.class);
	return candles.getBody();
    }

    @Override
    @Timed
    public OandaPrices getAllPrices(Set<OandaInstrument> instruments) throws UnsupportedEncodingException {
	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getPrices());
	StringBuilder sb = new StringBuilder();
	instruments.forEach(instrument -> sb.append(instrument.instrument).append("%2C"));
	URI uri = builder.queryParam("instruments", sb.toString()).build(true).toUri();
	ResponseEntity<OandaPrices> prices = null;
	try {
	    prices = restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaPrices.class);
	    LOG.debug("Got {}", prices.getBody().prices);
	} catch (HttpClientErrorException e) {
	    LOG.error("Client Error with the following body: {}", e.getResponseBodyAsString(), e);
	    return null;
	} catch (HttpServerErrorException e) {
	    LOG.warn("Server (Oanda) Error with the following body: {}", e.getResponseBodyAsString(), e);
	}
	return prices.getBody();
    }

    @Override
    @Timed
    public OandaOrderResponse sendOrder(OrderRequest request) {
	LOG.info("Sendning order to oanda: {}", request);
	MultiValueMap<String, String> oandaRequest = new OandaOrderRequest(request.instrument, request.units, request.side, request.type, request.expiry, request.price);
	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getOrders());
	// builder.queryParam("instrument", request.instrument)
	// .queryParam("units", request.units)
	// .queryParam("side", request.side)
	// .queryParam("type", request.type);
	// if(null != request.expiry) {
	// builder.queryParam("expiry", request.expiry);
	// }
	// if(null != request.price) {
	// builder.queryParam("price", request.price).toUriString();
	// }
	String uri = builder.buildAndExpand(request.externalDepotId).toUriString();
	LOG.info("Sending order to oanda: {}", uri);

	HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(oandaRequest, defaultHeaders);
	ResponseEntity<OandaOrderResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, OandaOrderResponse.class);
	return responseEntity.getBody();
    }

    @Override
    public Broker getBrokerID() {
	return Broker.OANDA;
    }
}
