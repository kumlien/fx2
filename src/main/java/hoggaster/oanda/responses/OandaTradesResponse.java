package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Created by svante2 on 2015-11-26.
 */
public class OandaTradesResponse {

    public final List<Trade> trades;

    @JsonCreator
    public OandaTradesResponse(@JsonProperty("trades") List<Trade> trades) {
        this.trades = Collections.unmodifiableList(trades);
    }


    public static class Trade {

        public final Long id;

        public final BigDecimal units;

        public final OrderSide side;

        public final CurrencyPair instrument;

        public final Instant time;

        public final BigDecimal price;

        public final BigDecimal takeProfit;

        public final BigDecimal stopLoss;

        public final BigDecimal trailingStop;

        public final BigDecimal trailingAmount;

        @JsonCreator
        public Trade(@JsonProperty("id")Long id, @JsonProperty("units")BigDecimal units, @JsonProperty("side")OrderSide side, @JsonProperty("instrument")CurrencyPair instrument, @JsonProperty("time")Instant time, @JsonProperty("price")BigDecimal price,
                     @JsonProperty("takeProfit")BigDecimal takeProfit, @JsonProperty("stopLoss") BigDecimal stopLoss, @JsonProperty("trailingStop") BigDecimal trailingStop, @JsonProperty("trailingAmount") BigDecimal trailingAmount) {
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
    }
}
