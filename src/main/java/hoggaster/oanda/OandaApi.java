package hoggaster.oanda;

import com.codahale.metrics.annotation.Timed;
import hoggaster.HttpConfig;
import hoggaster.depot.Position;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderService;
import hoggaster.oanda.requests.OandaOrderRequest;
import hoggaster.oanda.responses.OandaAccounts;
import hoggaster.oanda.responses.Instruments;
import hoggaster.oanda.responses.OandaAccount;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.oanda.responses.OandaInstrument;
import hoggaster.oanda.responses.OandaOrderResponse;
import hoggaster.oanda.responses.OandaPositions;
import hoggaster.oanda.responses.OandaPrices;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Access point to oanda
 */
public class OandaApi implements BrokerConnection, OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OandaApi.class);

    private final OandaResourcesProperties resources;

    private final RestTemplate restTemplate;

    private final RetryTemplate oandaRetryTemplate;

    private final HttpEntity<String> defaultHttpEntity;

    private final HttpHeaders defaultHeaders;

    @Autowired
    public OandaApi(OandaProperties oandaProps, RetryTemplate oandaRetryTemplate, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
        this.restTemplate = restTemplate;
        this.resources = resources;
        this.oandaRetryTemplate = oandaRetryTemplate;
        defaultHeaders = new HttpHeaders();
        defaultHeaders.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        defaultHeaders.set(HttpHeaders.CONNECTION, "Keep-Alive");
        defaultHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
        defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        defaultHttpEntity = new HttpEntity<>(defaultHeaders);
    }

    /**
     * Get all {@link OandaAccount}s connected to the configured api-key
     */
    @Override
    @Timed
    public OandaAccounts getAccounts() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getAccounts());
        String uri = builder.buildAndExpand("").toUriString();

        ResponseEntity<OandaAccounts> accounts = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getAccounts");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaAccounts.class);
                });
        LOG.info("Found {} accounts", accounts.getBody().getAccounts().size());
        accounts.getBody().getAccounts().forEach(a -> LOG.info("Account: {}", a));
        return accounts.getBody();
    }

    @Override
    @Timed
    public BrokerDepot getDepot(String depotId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getAccount());
        String uri = builder.buildAndExpand(depotId).toUriString();
        LOG.info("Get account from oanda with id {} using uri {}", depotId, uri);
        ResponseEntity<OandaAccount> account = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getAccount");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaAccount.class);
                });
        LOG.info("Found {} account", account.getBody());
        return account.getBody().toBrokerDepot();
    }

    @Override
    @Timed
    public List<Position> getPositions(String depotId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getPositions());
        String uri = builder.buildAndExpand(depotId).toUriString();
        LOG.info("Get positions from oanda with account id {} using uri {}", depotId, uri);
        ResponseEntity<OandaPositions> positions = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getPositions");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaPositions.class);
                });
        LOG.info("Found {} ", positions.getBody());
        return positions.getBody().positions
                .stream()
                .map(p -> new Position(p.instrument, p.side, p.units, p.avgPrice))
                .collect(toList());
    }

    /**
     * Get all available {@link CurrencyPair}s for the first account we find.
     *
     * @throws UnsupportedEncodingException
     */
    @Override
    @Timed
    public Instruments getInstrumentsForAccount(Integer accountId) throws UnsupportedEncodingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getInstruments());
        URI uri = builder.queryParam("accountId", accountId).queryParam("fields", "displayName%2Cinstrument%2Cpip%2CmaxTradeUnits%2Cprecision%2CmaxTrailingStop%2CminTrailingStop%2CmarginRate%2Chalted%2CinterestRate").build(true).toUri();
        LOG.debug("uri: {}", uri);

        ResponseEntity<Instruments> instruments = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getInstruments");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, Instruments.class);
                });
        LOG.debug("Got {}", instruments.getBody().getInstruments());
        return instruments.getBody();
    }

    /**
     * Fetch candles for the specified currencyPair.
     * <p>
     * count: Optional The number of candles to return in the response. This parameter may be ignored by the server depending on the time range provided. If not specified, count will default to 500. The maximum acceptable value for count is 5000. count should not be specified if both the start and end parameters are
     * also specified. start: Optional The start timestamp for the range of candles requested. The value specified must be in a valid datetime format. end: Optional The end timestamp for the range of candles requested. The value specified must be in a valid datetime format.
     *
     * @see http://developer.oanda.com/rest-practice/rates/#retrieveInstrumentHistory
     */
    @Override
    @Timed
    public OandaBidAskCandlesResponse getBidAskCandles(CurrencyPair currencyPair, CandleStickGranularity granularity, Integer count, Instant start, Instant end, boolean includeFirst) {
        if (includeFirst && start == null) {
            throw new IllegalArgumentException("Include first can only be set to true when start date is specified");
        }

        if (start != null && end != null && count != null && count > 0) {
            throw new IllegalArgumentException("All three parameters start date, end date and count can't be specified");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getCandles()).queryParam("instrument", currencyPair.name()).queryParam("granularity", granularity.oandaStyle);

        try {
            if (start != null) {
                String encoded = URLEncoder.encode(start.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
                builder.queryParam("start", encoded);
                builder.queryParam("includeFirst", includeFirst); //Only add this param is start is specified
            }
            if (end != null) {
                String encoded = URLEncoder.encode(end.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
                builder.queryParam("end", encoded);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (count != null) {
            builder.queryParam("count", count);
        }

        // String uri = builder.build(true).toUriString();
        URI uri = builder.build(true).toUri();
        LOG.debug("URI used: {}", uri);
        ResponseEntity<OandaBidAskCandlesResponse> candles = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getCandles");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaBidAskCandlesResponse.class);
                });
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
            prices = oandaRetryTemplate
                    .execute(context -> {
                        context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getAllPrices");
                        return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaPrices.class);
                    });
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
        MultiValueMap<String, String> oandaRequest = new OandaOrderRequest(request.currencyPair, request.units, request.side, request.type, request.expiry, request.price, request.getLowerBound(), request.getUpperBound());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getOrders());
        String uri = builder.buildAndExpand(request.externalDepotId).toUriString();
        LOG.info("Sending order to oanda: {}", uri);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(oandaRequest, defaultHeaders);
        ResponseEntity<OandaOrderResponse> orderResponse = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "sendOrder");
                    return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, OandaOrderResponse.class);
                });
        LOG.info("Received order response: {}", orderResponse.getBody());
        return orderResponse.getBody();
    }

    @Override
    public Broker getBrokerID() {
        return Broker.OANDA;
    }
}
