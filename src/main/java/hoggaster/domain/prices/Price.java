package hoggaster.domain.prices;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.brokers.Broker;
import hoggaster.oanda.responses.OandaPrice;
import hoggaster.rules.MarketUpdateType;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

import static hoggaster.domain.brokers.Broker.OANDA;

@Document
@CompoundIndexes({@CompoundIndex(name = "currencypair_time_idx", def = "{'currencyPair':1, 'time': 1}", unique = true)})
public class Price extends MarketUpdate {

    private String id;
    public final CurrencyPair currencyPair;
    public final BigDecimal bid;
    public final BigDecimal ask;
    public final Instant time;
    public final Broker broker;

    @PersistenceConstructor
    public Price(String id, CurrencyPair currencyPair, BigDecimal bid, BigDecimal ask, Instant time, Broker broker) {
        this.id = id;
        this.currencyPair = currencyPair;
        this.bid = bid;
        this.ask = ask;
        this.time = time;
        this.broker = broker;
    }

    public Price(CurrencyPair currencyPair, BigDecimal bid, BigDecimal ask, Instant time, Broker broker) {
        this.currencyPair = currencyPair;
        this.bid = bid;
        this.ask = ask;
        this.time = time;
        this.broker = broker;
    }

    public Price(OandaPrice p) {
        this.currencyPair = CurrencyPair.valueOf(p.instrument);
        this.bid = p.bid;
        this.ask = p.ask;
        this.time = p.time.toInstant();
        this.broker = OANDA;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Price [id=").append(id).append(", currencyPair=").append(currencyPair).append(", bid=").append(bid).append(", ask=").append(ask).append(", time=").append(time).append(", broker=").append(broker).append("]");
        return builder.toString();
    }

    @Override
    public MarketUpdateType getType() {
        return MarketUpdateType.PRICE;
    }



    public static class Builder {
        public CurrencyPair currencyPair;
        public BigDecimal bid;
        public BigDecimal ask;
        public Instant time;
        public Broker broker;

        private Builder() {
        }

        public static Builder aPrice() {
            return new Builder();
        }

        public Builder withCurrencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public Builder withBid(BigDecimal bid) {
            this.bid = bid;
            return this;
        }

        public Builder withAsk(BigDecimal ask) {
            this.ask = ask;
            return this;
        }

        public Builder withTime(Instant time) {
            this.time = time;
            return this;
        }

        public Builder withBroker(Broker broker) {
            this.broker = broker;
            return this;
        }

        public Builder but() {
            return aPrice().withCurrencyPair(currencyPair).withBid(bid).withAsk(ask).withTime(time).withBroker(broker);
        }

        public Price build() {
            Price price = new Price(currencyPair, bid, ask, time, broker);
            return price;
        }
    }
}
