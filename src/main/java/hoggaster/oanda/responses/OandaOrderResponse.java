package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OandaOrderResponse {

    public final CurrencyPair currencyPair;
    public final Instant time;
    public final Double price;

    @JsonIgnore
    public final Optional<TradeOpened> tradeOpened;

    @JsonIgnore
    public final Optional<OrderOpened> orderOpened;
    public final Optional<List<Trade>> tradesClosed; //In case of a sell order
    public final Optional<List<Trade>> tradesReduced; //In case of a sell order

    @JsonCreator
    public OandaOrderResponse(
            @JsonProperty(value = "instrument", required = true) CurrencyPair currencyPair,
            @JsonProperty(value = "price", required = true) Double price,
            @JsonProperty(value = "time", required = true) Instant time,
            @JsonProperty("tradeOpened") TradeOpened tradeOpened,
            @JsonProperty("orderOpened") OrderOpened orderOpened,
            @JsonProperty("tradesClosed") List<Trade> tradesClosed,
            @JsonProperty("tradesReduced") List<Trade> tradesReduced) {
        this.currencyPair = currencyPair;
        this.price = price;
        this.time = time;
        this.tradeOpened = Optional.ofNullable(tradeOpened);
        this.orderOpened = Optional.ofNullable(orderOpened);
        this.tradesClosed = Optional.ofNullable(tradesClosed);
        this.tradesReduced = Optional.ofNullable(tradesReduced);
    }

    public TradeOpened getTradeOpened() {
        return tradeOpened.isPresent() ? tradeOpened.get() : null;
    }

    public OrderOpened getOrderOpened() {
        return orderOpened.isPresent() ? orderOpened.get() : null;
    }

    public List<Trade> getTradesClosed() {
        return tradesClosed.isPresent() ? tradesClosed.get() : new ArrayList<>();
    }

    public List<Trade> getTradesReduced() {
        return tradesReduced.isPresent() ? tradesReduced.get() : new ArrayList<>();
    }


    public static class Trade {
        public final Long id;
        public final Long units;
        public final OrderSide side;

        @JsonCreator
        public Trade(
                @JsonProperty("id") Long id,
                @JsonProperty("units") Long units,
                @JsonProperty("side") OrderSide side) {
            this.id = id;
            this.units = units;
            this.side = side;
        }
    }


    public static class TradeOpened extends Trade {
        public final Long takeProfit;
        public final Long stopLoss;
        public final Long trailingStop;

        @JsonCreator
        public TradeOpened(
                @JsonProperty("id") Long id,
                @JsonProperty("units") Long units,
                @JsonProperty("side") OrderSide side,
                @JsonProperty("takeProfit") Long takeProfit,
                @JsonProperty("stopLoss") Long stopLoss,
                @JsonProperty("trailingStop") Long trailingStop) {
            super(id, units, side);
            this.takeProfit = takeProfit;
            this.stopLoss = stopLoss;
            this.trailingStop = trailingStop;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TradeOpened [id=").append(id).append(", units=")
                    .append(units).append(", side=").append(side)
                    .append(", takeProfit=").append(takeProfit)
                    .append(", stopLoss=").append(stopLoss)
                    .append(", trailingStop=").append(trailingStop).append("]");
            return builder.toString();
        }
    }


    public static class OrderOpened {
        private Long id;
        private Long lowerBound;
        private String side;
        private Long trailingStop;
        private Long takeProfit;
        private Long upperBound;
        private Long units;
        private Long stopLoss;
        private Long expiry;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(Long lowerBound) {
            this.lowerBound = lowerBound;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        public Long getTrailingStop() {
            return trailingStop;
        }

        public void setTrailingStop(Long trailingStop) {
            this.trailingStop = trailingStop;
        }

        public Long getTakeProfit() {
            return takeProfit;
        }

        public void setTakeProfit(Long takeProfit) {
            this.takeProfit = takeProfit;
        }

        public Long getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(Long upperBound) {
            this.upperBound = upperBound;
        }

        public Long getUnits() {
            return units;
        }

        public void setUnits(Long units) {
            this.units = units;
        }

        public Long getStopLoss() {
            return stopLoss;
        }

        public void setStopLoss(Long stopLoss) {
            this.stopLoss = stopLoss;
        }

        public Long getExpiry() {
            return expiry;
        }

        public void setExpiry(Long expiry) {
            this.expiry = expiry;
        }



    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OandaOrderResponse [currencyPair=").append(currencyPair)
                .append(", time=").append(time).append(", price=")
                .append(price).append(", tradeOpened=").append(tradeOpened)
                .append(", tradesClosed=").append(tradesClosed)
                .append(", tradesReduced=").append(tradesReduced).append("]");
        return builder.toString();
    }


}
