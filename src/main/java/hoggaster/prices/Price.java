package hoggaster.prices;

import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.domain.MarketUpdate;
import hoggaster.oanda.responses.OandaPrice;
import hoggaster.rules.MarketUpdateType;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public class Price extends MarketUpdate {

    private String id;
    public final Instrument instrument;
    public final Double bid;
    public final Double ask;
    public final Instant time;
    public final Broker broker;

    @PersistenceConstructor
    public Price(String id, Instrument instrument, Double bid, Double ask, Instant time, Broker broker) {
        this.id = id;
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.time = time;
        this.broker = broker;
    }

    public Price(Instrument instrument, Double bid, Double ask, Instant time, Broker broker) {
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.time = time;
        this.broker = broker;
    }

    public Price(OandaPrice p) {
        this.instrument = Instrument.valueOf(p.instrument);
        this.bid = p.bid;
        this.ask = p.ask;
        this.time = p.time.toInstant();
        this.broker = Broker.OANDA;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Price [id=").append(id).append(", instrument=").append(instrument).append(", bid=").append(bid).append(", ask=").append(ask).append(", time=").append(time).append(", broker=").append(broker).append("]");
        return builder.toString();
    }

    @Override
    public MarketUpdateType getType() {
        return MarketUpdateType.PRICE;
    }
}
