package hoggaster.candles;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.brokers.Broker;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.indicators.candles.CandleStickField;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/*
 * The bid price represents the maximum price that a buyer or buyers are willing to pay for a security.
 * The ask price represents the minimum price that a seller or sellers are willing to receive for the security
 */
@Document
@CompoundIndexes({@CompoundIndex(name = "currencypair_granularity_time_idx", def = "{'currencyPair':1, 'granularity':1, 'time': 1}", unique = true)})
public class Candle extends MarketUpdate {

    @Id
    private String id;

    public final CurrencyPair currencyPair;
    public final Broker brokerId;
    public final CandleStickGranularity granularity;

    public final Instant time;
    public final BigDecimal openBid;
    public final BigDecimal openAsk;
    public final BigDecimal highBid;
    public final BigDecimal highAsk;
    public final BigDecimal lowBid;
    public final BigDecimal lowAsk;
    public final BigDecimal closeBid;
    public final BigDecimal closeAsk;
    public final Integer volume;
    public final Boolean complete;

    @PersistenceConstructor
    Candle(String id, CurrencyPair currencyPair, Broker brokerId, CandleStickGranularity granularity, Instant time, BigDecimal openBid, BigDecimal openAsk, BigDecimal highBid, BigDecimal highAsk, BigDecimal lowBid, BigDecimal lowAsk, BigDecimal closeBid, BigDecimal closeAsk, Integer volume, Boolean complete) {
        this.id = id;
        this.currencyPair = currencyPair;
        this.brokerId = brokerId;
        this.granularity = granularity;
        this.time = time;
        this.openBid = openBid;
        this.openAsk = openAsk;
        this.highBid = highBid;
        this.highAsk = highAsk;
        this.lowBid = lowBid;
        this.lowAsk = lowAsk;
        this.closeBid = closeBid;
        this.closeAsk = closeAsk;
        this.volume = volume;
        this.complete = complete;
    }

    public Candle(CurrencyPair currencyPair, Broker brokerId, CandleStickGranularity granularity, Instant time, BigDecimal openBid, BigDecimal openAsk, BigDecimal highBid, BigDecimal highAsk, BigDecimal lowBid, BigDecimal lowAsk, BigDecimal closeBid, BigDecimal closeAsk, Integer volume, Boolean complete) {
        this(null, currencyPair, brokerId, granularity, time, openBid, openAsk, highBid, highAsk, lowBid, lowAsk, closeBid, closeAsk, volume, complete);
        this.id = generateId();
    }


    // Ugly thing to use our own generated id
    private String generateId() {
        return new StringBuilder().append(currencyPair.name()).append("|").append(granularity.name()).append("|").append(time.toString()).toString();
    }

    public String getId() {
        return id;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
        result = prime * result + ((currencyPair == null) ? 0 : currencyPair.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Candle other = (Candle) obj;
        if (granularity != other.granularity)
            return false;
        if (currencyPair != other.currencyPair)
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BidAskCandle [id=").append(getId()).append(", currencyPair=").append(currencyPair).append(", brokerId=").append(brokerId).append(", granularity=").append(granularity).append(", time=").append(time).append(", openBid=").append(openBid).append(", openAsk=").append(openAsk).append(", highBid=")
                .append(highBid).append(", highAsk=").append(highAsk).append(", lowBid=").append(lowBid).append(", lowAsk=").append(lowAsk).append(", closeBid=").append(closeBid).append(", closeAsk=").append(closeAsk).append(", volume=").append(volume).append(", complete=").append(complete).append("]");
        return builder.toString();
    }

    @Override
    public MarketUpdateType getType() {
        return granularity.type;
    }

    public BigDecimal getValue(CandleStickField field) {
        switch (field) {
            case CLOSE_ASK:
                return closeAsk;
            case CLOSE_BID:
                return closeBid;
            case HIGH_ASK:
                return highAsk;
            case HIGH_BID:
                return highBid;
            case LOW_ASK:
                return lowAsk;
            case LOW_BID:
                return lowBid;
            case OPEN_ASK:
                return openAsk;
            case OPEN_BID:
                return openBid;
            default:
                throw new IllegalArgumentException("Unknown candlestick field " + field);
        }
    }

    public static final class Builder {
        final public CurrencyPair currencyPair;
        public Broker brokerId;
        final public CandleStickGranularity granularity;
        final public Instant time;
        public BigDecimal openBid;
        public BigDecimal openAsk;
        public BigDecimal highBid;
        public BigDecimal highAsk;
        public BigDecimal lowBid;
        public BigDecimal lowAsk;
        public BigDecimal closeBid;
        public BigDecimal closeAsk;
        public Integer volume;
        public Boolean complete;


        public Builder(CurrencyPair currencyPair, CandleStickGranularity granularity, Instant time) {
            this.currencyPair = currencyPair;
            this.granularity = granularity;
            this.time = time;

        }

        public static Builder aCandle(CurrencyPair currencyPair, CandleStickGranularity granularity, Instant time) {
            return new Builder(currencyPair, granularity, time);
        }

        public Builder withBrokerId(Broker brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public Builder withOpenBid(BigDecimal openBid) {
            this.openBid = openBid;
            return this;
        }

        public Builder withOpenAsk(BigDecimal openAsk) {
            this.openAsk = openAsk;
            return this;
        }

        public Builder withHighBid(BigDecimal highBid) {
            this.highBid = highBid;
            return this;
        }

        public Builder withHighAsk(BigDecimal highAsk) {
            this.highAsk = highAsk;
            return this;
        }

        public Builder withLowBid(BigDecimal lowBid) {
            this.lowBid = lowBid;
            return this;
        }

        public Builder withLowAsk(BigDecimal lowAsk) {
            this.lowAsk = lowAsk;
            return this;
        }

        public Builder withCloseBid(BigDecimal closeBid) {
            this.closeBid = closeBid;
            return this;
        }

        public Builder withCloseAsk(BigDecimal closeAsk) {
            this.closeAsk = closeAsk;
            return this;
        }

        public Builder withVolume(Integer volume) {
            this.volume = volume;
            return this;
        }

        public Builder withComplete(Boolean complete) {
            this.complete = complete;
            return this;
        }

        public Candle build() {
            Candle candle = new Candle(currencyPair, brokerId, granularity, time, openBid, openAsk, highBid, highAsk, lowBid, lowAsk, closeBid, closeAsk, volume, complete);
            return candle;
        }
    }
}
