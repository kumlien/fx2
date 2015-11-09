package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.time.Instant;
import java.util.List;

public class OandaOrderResponse {

    public final CurrencyPair currencyPair;
    public final Instant time;
    public final Double price;
    public final TradeOpened tradeOpened;
    public final List<Trade> tradesClosed; //In case of a sell order
    public final List<Trade> tradesReduced; //In case of a sell order

    @JsonCreator
    public OandaOrderResponse(
            @JsonProperty("currencyPair") CurrencyPair currencyPair,
            @JsonProperty("price") Double price,
            @JsonProperty("time") Instant time,
            @JsonProperty("tradeOpened") TradeOpened tradeOpened,
            @JsonProperty(value = "tradesClosed", required = false) List<Trade> tradesClosed,
            @JsonProperty(value = "tradesReduced", required = false) List<Trade> tradesReduced) {
        this.currencyPair = currencyPair;
        this.price = price;
        this.time = time;
        this.tradeOpened = tradeOpened;
        this.tradesClosed = tradesClosed;
        this.tradesReduced = tradesReduced;
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
