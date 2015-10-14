package hoggaster.prices;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.brokers.Broker;
import hoggaster.oanda.responses.OandaPrice;
import hoggaster.rules.MarketUpdateType;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@CompoundIndexes({@CompoundIndex(name = "currencypair_time_idx", def = "{'currencyPair':1, 'time': 1}", unique = true)})
public class Price extends MarketUpdate {

    private String id;
    public final CurrencyPair currencyPair;
    public final Double bid;
    public final Double ask;
    public final Instant time;
    public final Broker broker;

    @PersistenceConstructor
    public Price(String id, CurrencyPair currencyPair, Double bid, Double ask, Instant time, Broker broker) {
        this.id = id;
        this.currencyPair = currencyPair;
        this.bid = bid;
        this.ask = ask;
        this.time = time;
        this.broker = broker;
    }

    public Price(CurrencyPair currencyPair, Double bid, Double ask, Instant time, Broker broker) {
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
        this.broker = Broker.OANDA;
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
}
