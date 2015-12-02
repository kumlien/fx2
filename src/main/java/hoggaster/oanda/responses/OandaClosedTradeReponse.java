package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by svante.kumlien on 02.12.15.
 */
public class OandaClosedTradeReponse {

     // The ID of the close trade transaction (on oanda side)
    public final Integer id;

    public final BigDecimal price; // The price the trade was closed at

    // The symbol of the instrument of the trade
    public final CurrencyPair instrument;

    // The realized profit of the trade in units of base currency
    public final BigDecimal profit;

    // The direction the trade was in
    public final OrderSide side;

    // The time at which the trade was closed (in RFC3339 format)
    public final Instant time;

    @JsonCreator
    public OandaClosedTradeReponse(@JsonProperty("id") Integer id, @JsonProperty("price")BigDecimal price, @JsonProperty("instrument") CurrencyPair instrument,  @JsonProperty("profit") BigDecimal profit, @JsonProperty("side") OrderSide side,  @JsonProperty("time") Instant time) {
        this.id = id;
        this.price = price;
        this.instrument = instrument;
        this.profit = profit;
        this.side = side;
        this.time = time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("price", price)
                .add("instrument", instrument)
                .add("profit", profit)
                .add("side", side)
                .add("time", time)
                .toString();
    }
}
