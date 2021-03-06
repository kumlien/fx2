package hoggaster.domain.prices;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import hoggaster.domain.CurrencyPair;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Carries a Tick.
 *
 * Created by svante.kumlien on 04.03.16.
 */
public final class TickContainer {
    public final Tick tick;

    @JsonCreator
    public TickContainer(@JsonProperty("tick") Tick tick){
        this.tick = tick;
    }

    public static final class Tick {
        public final CurrencyPair instrument;
        public final Date time;
        public final BigDecimal bid;
        public final BigDecimal ask;

        @JsonCreator
        public Tick(@JsonProperty("instrument") CurrencyPair instrument, @JsonProperty("time") Date time, @JsonProperty("bid") BigDecimal bid, @JsonProperty("ask") BigDecimal ask){
            this.instrument = instrument;
            this.time = time;
            this.bid = bid;
            this.ask = ask;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("instrument", instrument)
                    .add("time", time)
                    .add("bid", bid)
                    .add("ask", ask)
                    .toString();
        }

    }
}
