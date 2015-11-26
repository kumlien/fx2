package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;

/**
 * Created by svante.kumlien on 11.11.15.
 */
public class OandaPosition {

    public final BigDecimal avgPrice;
    public final OrderSide side;
    public final CurrencyPair instrument;
    public final BigDecimal units;

    @JsonCreator
    public OandaPosition(@JsonProperty(value = "avgPrice") BigDecimal avgPrice, @JsonProperty(value = "side") OrderSide side, @JsonProperty(value = "instrument") CurrencyPair instrument, @JsonProperty(value = "units") BigDecimal units) {
        this.avgPrice = avgPrice;
        this.side = side;
        this.instrument = instrument;
        this.units = units;
    }

    @Override
    public String toString() {
        return "OandaPosition{" +
                "avgPrice=" + avgPrice +
                ", side=" + side +
                ", instrument=" + instrument +
                ", units=" + units +
                '}';
    }
}
