package hoggaster.domain.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A order request, right now hard coded to provide what oanda needs.
 */
public class OrderRequest {

    public final String externalDepotId;

    public final CurrencyPair currencyPair;

    public final Long units;

    public final OrderSide side;

    public final OrderType type;

    @JsonInclude(Include.NON_NULL)
    public final Instant expiry;

    public final BigDecimal price;


    //Optional fields
    @JsonInclude(Include.NON_NULL)
    private Double stopLoss;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal takeProfit;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal trailingStop;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal lowerBound;

    @JsonInclude(Include.NON_NULL)
    private BigDecimal upperBound;

    @JsonCreator
    public OrderRequest(
            @JsonProperty("externalDepotId") String externalDepotId,
            @JsonProperty("currencyPair") CurrencyPair currencyPair,
            @JsonProperty("units") Long units,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("type") OrderType type,
            @JsonProperty(value = "expiry", required = false) Instant expiry,
            @JsonProperty(value = "price", required = false) BigDecimal price) {
        this.externalDepotId = externalDepotId;
        this.currencyPair = currencyPair;
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

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }

    public BigDecimal getTrailingStop() {
        return trailingStop;
    }

    public void setTrailingStop(BigDecimal trailingStop) {
        this.trailingStop = trailingStop;
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(BigDecimal upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(BigDecimal lowerBound) {
        this.lowerBound = lowerBound;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderRequest{");
        sb.append("externalDepotId='").append(externalDepotId).append('\'');
        sb.append(", currencyPair=").append(currencyPair);
        sb.append(", units=").append(units);
        sb.append(", side=").append(side);
        sb.append(", type=").append(type);
        sb.append(", expiry=").append(expiry);
        sb.append(", price=").append(price);
        sb.append(", stopLoss=").append(stopLoss);
        sb.append(", takeProfit=").append(takeProfit);
        sb.append(", trailingStop=").append(trailingStop);
        sb.append(", lowerBound=").append(lowerBound);
        sb.append(", upperBound=").append(upperBound);
        sb.append('}');
        return sb.toString();
    }
}
