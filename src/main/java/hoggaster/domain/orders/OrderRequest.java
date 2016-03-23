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

    public static class Builder {
        public String externalDepotId;
        public CurrencyPair currencyPair;
        public Long units;
        public OrderSide side;
        public OrderType type;
        public Instant expiry;
        public BigDecimal price;
        //Optional fields
        private Double stopLoss;
        private BigDecimal takeProfit;
        private BigDecimal trailingStop;
        private BigDecimal lowerBound;
        private BigDecimal upperBound;

        private Builder() {
        }

        public static Builder anOrderRequest() {
            return new Builder();
        }

        public Builder withStopLoss(Double stopLoss) {
            this.stopLoss = stopLoss;
            return this;
        }

        public Builder withTakeProfit(BigDecimal takeProfit) {
            this.takeProfit = takeProfit;
            return this;
        }

        public Builder withTrailingStop(BigDecimal trailingStop) {
            this.trailingStop = trailingStop;
            return this;
        }

        public Builder withLowerBound(BigDecimal lowerBound) {
            this.lowerBound = lowerBound;
            return this;
        }

        public Builder withUpperBound(BigDecimal upperBound) {
            this.upperBound = upperBound;
            return this;
        }

        public Builder withExternalDepotId(String externalDepotId) {
            this.externalDepotId = externalDepotId;
            return this;
        }

        public Builder withCurrencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public Builder withUnits(Long units) {
            this.units = units;
            return this;
        }

        public Builder withSide(OrderSide side) {
            this.side = side;
            return this;
        }

        public Builder withType(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder withExpiry(Instant expiry) {
            this.expiry = expiry;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder but() {
            return anOrderRequest().withStopLoss(stopLoss).withTakeProfit(takeProfit).withTrailingStop(trailingStop).withLowerBound(lowerBound).withUpperBound(upperBound).withExternalDepotId(externalDepotId).withCurrencyPair(currencyPair).withUnits(units).withSide(side).withType(type).withExpiry(expiry).withPrice(price);
        }

        public OrderRequest build() {
            OrderRequest orderRequest = new OrderRequest(externalDepotId, currencyPair, units, side, type, expiry, price);
            orderRequest.setStopLoss(stopLoss);
            orderRequest.setTakeProfit(takeProfit);
            orderRequest.setTrailingStop(trailingStop);
            orderRequest.setLowerBound(lowerBound);
            orderRequest.setUpperBound(upperBound);
            return orderRequest;
        }
    }
}
