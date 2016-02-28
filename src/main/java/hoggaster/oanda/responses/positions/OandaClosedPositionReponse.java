package hoggaster.oanda.responses.positions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import hoggaster.domain.CurrencyPair;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by svante.kumlien on 02.12.15.
 */
public class OandaClosedPositionReponse {

     // The ID of the close trade transaction (on oanda side)
    public final List<Integer> ids;

    public final BigDecimal price; // The price the trade was closed at

    // The symbol of the instrument of the trade
    public final CurrencyPair instrument;

    // The realized profit of the trade in units of base currency
    public final BigDecimal totalUnits;


    @JsonCreator
    public OandaClosedPositionReponse(@JsonProperty("ids") List<Integer> ids, @JsonProperty("price")BigDecimal price, @JsonProperty("instrument") CurrencyPair instrument, @JsonProperty("totalUnits") BigDecimal totalUnits) {
        this.ids = ids;
        this.price = price;
        this.instrument = instrument;
        this.totalUnits = totalUnits;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ids", ids)
                .add("price", price)
                .add("instrument", instrument)
                .add("totalUnits", totalUnits)
                .toString();
    }
}
