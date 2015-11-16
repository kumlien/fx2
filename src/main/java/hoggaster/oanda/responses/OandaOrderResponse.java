package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.trades.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The response returned from an order request sent to Oanda
 */
public class OandaOrderResponse implements OrderResponse {

    private final CurrencyPair currencyPair;
    private final Instant time;
    private final BigDecimal price;

    @JsonIgnore
    private final Optional<OandaTradeOpened> tradeOpened;

    @JsonIgnore
    private final Optional<OrderOpened> orderOpened;
    private final Optional<List<OandaTrade>> tradesClosed;
    private final Optional<List<OandaTrade>> tradesReduced;

    @JsonCreator
    public OandaOrderResponse(
            @JsonProperty(value = "instrument", required = true) CurrencyPair currencyPair,
            @JsonProperty(value = "price", required = true) BigDecimal price,
            @JsonProperty(value = "time", required = true) Instant time,
            @JsonProperty("tradeOpened") OandaTradeOpened oandaTradeOpened,
            @JsonProperty("orderOpened") OrderOpened orderOpened,
            @JsonProperty("tradesClosed") List<OandaTrade> tradesClosed,
            @JsonProperty("tradesReduced") List<OandaTrade> tradesReduced) {
        this.currencyPair = currencyPair;
        this.price = price;
        this.time = time;
        this.tradeOpened = Optional.ofNullable(oandaTradeOpened);
        this.orderOpened = Optional.ofNullable(orderOpened);
        this.tradesClosed = Optional.ofNullable(tradesClosed);
        this.tradesReduced = Optional.ofNullable(tradesReduced);


    }

    public OrderOpened getOrderOpened() {
        return orderOpened.isPresent() ? orderOpened.get() : null;
    }

    public List<OandaTrade> getTradesClosed() {
        return tradesClosed.isPresent() ? tradesClosed.get() : new ArrayList<>();
    }

    public List<OandaTrade> getTradesReduced() {
        return tradesReduced.isPresent() ? tradesReduced.get() : new ArrayList<>();
    }

    @Override
    public boolean tradeWasOpened() {
        return tradeOpened.isPresent();
    }

    @Override
    public Optional<Trade> getOpenedTrade(String depotId, String robotId) {
        if(tradeOpened.isPresent()) {
            OandaTradeOpened ot = tradeOpened.get();
            return Optional.of(new Trade(depotId, robotId, Broker.OANDA, ot.id, ot.units, ot.side, currencyPair, time, price, ot.takeProfit, ot.stopLoss, ot.trailingStop));
        } else {
            return Optional.empty();
        }
    }


    public static class OandaTrade {
        public final Long id;
        public final BigDecimal units;
        public final OrderSide side;

        @JsonCreator
        public OandaTrade(
                @JsonProperty("id") Long id,
                @JsonProperty("units") BigDecimal units,
                @JsonProperty("side") OrderSide side) {
            this.id = id;
            this.units = units;
            this.side = side;
        }
    }


    public static class OandaTradeOpened extends OandaTrade {
        public final BigDecimal takeProfit;
        public final BigDecimal stopLoss;
        public final BigDecimal trailingStop;

        @JsonCreator
        public OandaTradeOpened(
                @JsonProperty("id") Long id,
                @JsonProperty("units") BigDecimal units,
                @JsonProperty("side") OrderSide side,
                @JsonProperty("takeProfit") BigDecimal takeProfit,
                @JsonProperty("stopLoss") BigDecimal stopLoss,
                @JsonProperty("trailingStop") BigDecimal trailingStop) {
            super(id, units, side);
            this.takeProfit = takeProfit;
            this.stopLoss = stopLoss;
            this.trailingStop = trailingStop;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("OandaTradeOpened [id=").append(id).append(", units=")
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
