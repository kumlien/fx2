package hoggaster.domain.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.Instrument;

import java.time.Instant;

public class OrderRequest {

    public final String externalDepotId;

    public final Instrument instrument;

    public final Long units;

    public final OrderSide side;

    public final OrderType type;

    public final Instant expiry;

    public final Double price;


    //Optional fields
    @JsonInclude(Include.NON_NULL)
    private Double stopLoss;

    @JsonInclude(Include.NON_NULL)
    private Double takeProfit;

    @JsonInclude(Include.NON_NULL)
    private Double trailingStop;


    @JsonInclude(Include.NON_NULL)
    public Double lowerBound;

    @JsonInclude(Include.NON_NULL)
    public Double upperBound;

    @JsonCreator
    public OrderRequest(
            @JsonProperty("externalDepotId") String externalDepotId,
            @JsonProperty("instrument") Instrument instrument,
            @JsonProperty("units") Long units,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("type") OrderType type,
            @JsonProperty(value = "expiry", required = false) Instant expiry,
            @JsonProperty(value = "price", required = false) Double price) {
        this.externalDepotId = externalDepotId;
        this.instrument = instrument;
        this.units = units;
        this.side = side;
        this.type = type;
        this.expiry = expiry;
        this.price = price;
    }

    public Double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public Double getTrailingStop() {
        return trailingStop;
    }

    public void setTrailingStop(Double trailingStop) {
        this.trailingStop = trailingStop;
    }

    public Double getLowerBound() {
        return lowerBound;
    }

    public Double getUpperBound() {
        return upperBound;
    }
}
