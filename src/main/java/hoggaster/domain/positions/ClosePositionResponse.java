package hoggaster.domain.positions;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Returned from a Broker in response to a request to close a specific position.
 *
 * @author  svante.kumlien
 */
public class ClosePositionResponse {

    public final Broker broker;

    /**
     * List of transaction id.s which was the result of the close position request
     */
    public final List<String> transactionIds;

    public final BigDecimal price;

    public final CurrencyPair currencyPair;

    public final BigDecimal totalUnits;

    public final Instant time;


    public ClosePositionResponse(Broker broker, List<String> transactionIds, BigDecimal price, CurrencyPair currencyPair, BigDecimal totalUnits, Instant time) {
        this.broker = broker;
        this.transactionIds = Collections.unmodifiableList(transactionIds);
        this.price = price;
        this.currencyPair = currencyPair;
        this.totalUnits = totalUnits;
        this.time = time;
    }


    public static class ClosePositionResponseBuilder {
        public Broker broker;
        public List<String> transactionIds;
        public BigDecimal price;
        public CurrencyPair currencyPair;
        public BigDecimal totalUnits;
        public Instant time;

        private ClosePositionResponseBuilder() {
        }

        public static ClosePositionResponseBuilder aClosePositionResponse() {
            return new ClosePositionResponseBuilder();
        }

        public ClosePositionResponseBuilder withBroker(Broker broker) {
            this.broker = broker;
            return this;
        }

        public ClosePositionResponseBuilder withTransactionIds(List<String> transactionIds) {
            this.transactionIds = transactionIds;
            return this;
        }

        public ClosePositionResponseBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ClosePositionResponseBuilder withCurrencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public ClosePositionResponseBuilder withTotalUnits(BigDecimal totalUnits) {
            this.totalUnits = totalUnits;
            return this;
        }

        public ClosePositionResponseBuilder withTime(Instant time) {
            this.time = time;
            return this;
        }

        public ClosePositionResponseBuilder but() {
            return aClosePositionResponse().withBroker(broker).withTransactionIds(transactionIds).withPrice(price).withCurrencyPair(currencyPair).withTotalUnits(totalUnits).withTime(time);
        }

        public ClosePositionResponse build() {
            ClosePositionResponse closePositionResponse = new ClosePositionResponse(broker, transactionIds, price, currencyPair, totalUnits, time);
            return closePositionResponse;
        }
    }
}
