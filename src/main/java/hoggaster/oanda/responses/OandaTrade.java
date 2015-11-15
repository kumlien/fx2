package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by svante2 on 2015-11-15.
 */
public class OandaTrade {

    public final Integer id;

    public final Integer units;

    public final String side;

    public final CurrencyPair instrument;

    public final Instant time;

    public final BigDecimal price;

    public final BigDecimal takeProfit;

    public final BigDecimal stopLoss;

    public final BigDecimal trailingStop;

    public final BigDecimal trailingAmount;

    /**
     *
     * @param trailingAmount
     * @param id
     * @param time
     * @param price
     * @param side
     * @param trailingStop
     * @param instrument
     * @param takeProfit
     * @param units
     * @param stopLoss
     */
    @JsonCreator
    public OandaTrade(@JsonProperty("id") Integer id, @JsonProperty("units") Integer units,@JsonProperty("side") String side,@JsonProperty("instrument") CurrencyPair instrument,@JsonProperty("time") Instant time,
                      @JsonProperty("price") BigDecimal price, @JsonProperty("takeProfit") BigDecimal takeProfit, @JsonProperty("stopLoss") BigDecimal stopLoss, @JsonProperty("trailingStop") BigDecimal trailingStop,  @JsonProperty("trailingAmount") BigDecimal trailingAmount) {
        this.id = id;
        this.units = units;
        this.side = side;
        this.instrument = instrument;
        this.time = time;
        this.price = price;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.trailingStop = trailingStop;
        this.trailingAmount = trailingAmount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
