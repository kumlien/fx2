package hoggaster.domain.trades;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Returned from a Broker in response to a request to close a specific trade.
 *
 * @author  svante.kumlien
 */
public class CloseTradeResponse {

    public final Broker broker;

    public final String brokerTradeId;

    public final BigDecimal price;

    public final CurrencyPair currencyPair;

    public final BigDecimal profit;

    public final OrderSide side;

    public final Instant time;


    public CloseTradeResponse(Broker broker, String brokerTradeId, BigDecimal price, CurrencyPair currencyPair, BigDecimal profit, OrderSide side, Instant time) {
        this.broker = broker;
        this.brokerTradeId = brokerTradeId;
        this.price = price;
        this.currencyPair = currencyPair;
        this.profit = profit;
        this.side = side;
        this.time = time;
    }

    public static class CloseTradeResponseBuilder {
        public Broker broker;
        public String brokerTradeId;
        public BigDecimal price;
        public CurrencyPair currencyPair;
        public BigDecimal profit;
        public OrderSide side;
        public Instant time;

        private CloseTradeResponseBuilder() {
        }

        public static CloseTradeResponseBuilder aCloseTradeResponse() {
            return new CloseTradeResponseBuilder();
        }

        public CloseTradeResponseBuilder withBroker(Broker broker) {
            this.broker = broker;
            return this;
        }

        public CloseTradeResponseBuilder withBrokerTradeId(String brokerTradeId) {
            this.brokerTradeId = brokerTradeId;
            return this;
        }

        public CloseTradeResponseBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CloseTradeResponseBuilder withCurrencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public CloseTradeResponseBuilder withProfit(BigDecimal profit) {
            this.profit = profit;
            return this;
        }

        public CloseTradeResponseBuilder withSide(OrderSide side) {
            this.side = side;
            return this;
        }

        public CloseTradeResponseBuilder withTime(Instant time) {
            this.time = time;
            return this;
        }

        public CloseTradeResponseBuilder but() {
            return aCloseTradeResponse().withBroker(broker).withBrokerTradeId(brokerTradeId).withPrice(price).withCurrencyPair(currencyPair).withProfit(profit).withSide(side).withTime(time);
        }

        public CloseTradeResponse build() {
            CloseTradeResponse closeTradeResponse = new CloseTradeResponse(broker, brokerTradeId, price, currencyPair, profit, side, time);
            return closeTradeResponse;
        }
    }
}
