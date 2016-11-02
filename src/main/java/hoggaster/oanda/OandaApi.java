package hoggaster.oanda;

import com.codahale.metrics.annotation.Timed;
import hoggaster.HttpConfig;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.positions.ClosePositionResponse;
import hoggaster.domain.positions.Position;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.requests.OandaOrderRequest;
import hoggaster.oanda.responses.*;
import hoggaster.oanda.responses.positions.OandaClosedPositionReponse;
import hoggaster.oanda.responses.positions.OandaPositions;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hoggaster.domain.brokers.Broker.OANDA;
import static hoggaster.domain.positions.ClosePositionResponse.ClosePositionResponseBuilder.aClosePositionResponse;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

/**
 * Access point to oanda.
 *
 * @see hoggaster.OandaConnectionConfig
 */
public class OandaApi implements BrokerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(OandaApi.class);

    private final OandaResourcesProperties resources;

    private final RestTemplate restTemplate;

    private final RetryTemplate oandaRetryTemplate;

    private final HttpEntity<String> defaultHttpEntity;

    private final HttpHeaders defaultHeaders;

    public OandaApi(OandaProperties oandaProps, RetryTemplate oandaRetryTemplate, RestTemplate restTemplate, OandaResourcesProperties resources) throws UnsupportedEncodingException {
        this.restTemplate = restTemplate;
        this.resources = resources;
        this.oandaRetryTemplate = oandaRetryTemplate;
        defaultHeaders = new HttpHeaders();
        defaultHeaders.set(ACCEPT_ENCODING, "gzip, deflate");
        defaultHeaders.set(CONNECTION, "Keep-Alive");
        defaultHeaders.set(AUTHORIZATION, "Bearer " + oandaProps.getApiKey());
        defaultHeaders.setContentType(APPLICATION_FORM_URLENCODED);
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
    public Set<Trade> getOpenTrades(String fx2DepotId, String brokerDepotId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getTrades());
        String uri = builder.buildAndExpand(brokerDepotId).toUriString();
        LOG.info("Get open trades from oanda depot with id {} using uri {}", brokerDepotId, uri);
        ResponseEntity<OandaTradesResponse> openTrades = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getNumberOfOpenTrades");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaTradesResponse.class);
                });
        return openTrades.getBody().trades.stream()
                .map(t -> fromOandaTrade(fx2DepotId, t))
                .collect(toSet());
    }

    @Override
    public Optional<Trade> getTrade(String fx2DepotId, String brokerDepotId, String tradeId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getTrade());
        String uri = builder.buildAndExpand(brokerDepotId, tradeId).toUriString();
        LOG.info("Get trade by id from oanda for depot with id {}, oandaTradeId: {} using uri {}", brokerDepotId, tradeId, uri);
        ResponseEntity<OandaTradesResponse.Trade> openTrade = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getTrade");
                    return restTemplate.exchange(uri, HttpMethod.GET, defaultHttpEntity, OandaTradesResponse.Trade.class);
                });
        OandaTradesResponse.Trade trade = openTrade.getBody();
        if (trade != null) {
            return Optional.of(fromOandaTrade(fx2DepotId, trade));
        }
        return Optional.empty();
    }


    @Override
    public CloseTradeResponse closeTrade(Trade trade, String brokerAccountId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getTrade());
        String uri = builder.buildAndExpand(brokerAccountId, trade.brokerId).toUriString();
        LOG.info("Close trade by id from oanda for depot with id {}, oandaTradeId: {} using uri {}", trade.depotId, trade.brokerId, uri);
        ResponseEntity<OandaClosedTradeReponse> closedTradeEntity = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "closeTrade");
                    return restTemplate.exchange(uri, HttpMethod.DELETE, defaultHttpEntity, OandaClosedTradeReponse.class);
                });
        OandaClosedTradeReponse closedTrade = closedTradeEntity.getBody();

        return new CloseTradeResponse(OANDA, String.valueOf(closedTrade.id), closedTrade.price, closedTrade.instrument, closedTrade.profit, closedTrade.side, closedTrade.time);
    }

    private static final Trade fromOandaTrade(String fx2DepotId, OandaTradesResponse.Trade t) {
        return new Trade(fx2DepotId, null, OANDA, t.id, t.units, t.side, t.instrument, t.time, t.price, t.takeProfit, t.stopLoss, t.trailingStop);
    }

    @Override
    @Timed
    public Set<Position> getPositions(String depotId) {
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
                .collect(toSet());
    }

    /**
     * Close the position for the specified instrument and account
     *
     * @throws UnsupportedEncodingException
     */
    @Override
    @Timed
    public ClosePositionResponse closePosition(Integer accountId, CurrencyPair instrument) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getPosition());
        String uri = builder.buildAndExpand(accountId, instrument).toUriString();
        LOG.info("Close position from oanda with account id {} and instrument {} using uri {}", accountId, instrument, uri);
        ResponseEntity<OandaClosedPositionReponse> closedPosition = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "closePosition");
                    return restTemplate.exchange(uri, HttpMethod.DELETE, defaultHttpEntity, OandaClosedPositionReponse.class);
                });

        OandaClosedPositionReponse response = closedPosition.getBody();
        LOG.info("Response from close position {} ", response);
        return aClosePositionResponse().withBroker(OANDA).withCurrencyPair(response.instrument).withPrice(response.price).withTime(Instant.now())
                .withTotalUnits(response.totalUnits)
                .withTransactionIds(response.ids.stream().map(String::valueOf).collect(Collectors.toList()))
                .build();

    }


    /**
     * Get all available {@link CurrencyPair}s for the specified account
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
                builder.queryParam("includeFirst", includeFirst); //Only tradeOpened this param is start is specified
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
    public OandaPrices getPrices(Set<OandaInstrument> instruments) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getPrices());
        StringBuilder sb = new StringBuilder();
        instruments.forEach(instrument -> sb.append(instrument.instrument).append("%2C"));
        URI uri = builder.queryParam("instruments", sb.toString()).build(true).toUri();
        ResponseEntity<OandaPrices> prices = null;
        try {
            prices = oandaRetryTemplate
                    .execute(context -> {
                        context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "getPrices");
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
    public Observable<OandaPrices> getPricesAsync(Set<OandaInstrument> instruments) {
        return Observable.defer(() -> Observable.just(getPrices(instruments))).subscribeOn(Schedulers.io());
    }

    @Override
    @Timed
    public OrderResponse sendOrder(OrderRequest request) {
        MultiValueMap<String, String> oandaRequest = new OandaOrderRequest(request.currencyPair, request.units, request.side, request.type, request.expiry, request.price, request.getLowerBound(), request.getUpperBound());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resources.getOrders());
        String uri = builder.buildAndExpand(request.externalDepotId).toUriString();
        LOG.info("Sendning order to {}: {}", uri, request);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(oandaRequest, defaultHeaders);
        ResponseEntity<OandaCreateTradeResponse> orderResponse = oandaRetryTemplate
                .execute(context -> {
                    context.setAttribute(HttpConfig.OANDA_CALL_CTX_ATTR, "sendOrder");
                    return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, OandaCreateTradeResponse.class);
                });
        LOG.info("Received order response: {}", orderResponse.getBody());
        return orderResponse.getBody();
    }

    @Override
    public Broker getBrokerID() {
        return OANDA;
    }
}
