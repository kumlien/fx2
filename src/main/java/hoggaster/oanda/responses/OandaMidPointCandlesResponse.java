package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Oanda representation of a json response containing a candle.
 */
public class OandaMidPointCandlesResponse {

    public final String instrument;
    public final String granularity;
    public final List<OandaMidPointCandle> candles;

    @JsonCreator
    public OandaMidPointCandlesResponse(@JsonProperty(value = "currencyPair") String instrument, @JsonProperty(value = "granularity") String granularity,
                                        @JsonProperty(value = "candles") List<OandaMidPointCandle> candles) {
        this.instrument = instrument;
        this.granularity = granularity;
        this.candles = candles;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InstrumentHistory [currencyPair=").append(instrument)
                .append(", granularity=").append(granularity)
                .append(", candles=").append(candles).append("]");
        return builder.toString();
    }
}
