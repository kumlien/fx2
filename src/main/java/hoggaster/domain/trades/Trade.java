package hoggaster.domain.trades;

import com.google.common.base.MoreObjects;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderSide;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a single Trade. When a trade is open it lives in the Depot document. Once it is closed
 * it is moved to it's own collection.
 *
 * Created by svante2 on 2015-11-15.
 */
@Document (collection = "historic_trades")
public class Trade {

    @Id
    private String id;

    private TradeStatus status;

    public final String depotId;

    public final String robotId;

    public final Broker broker;

    /**
     * The id on the broker side for this trade
     */
    public final Long brokerId;

    public final BigDecimal units;

    public final OrderSide side;

    public final CurrencyPair instrument;

    public final Instant openTime;

    public final BigDecimal openPrice;

    public final BigDecimal takeProfit;

    public final BigDecimal stopLoss;

    public final BigDecimal trailingStop;

    public final BigDecimal trailingAmount;

    //Below stuff set when trade is closed.
    private BigDecimal closePrice;

    private BigDecimal gainPerUnit;

    private BigDecimal totalGain;

    private Instant closeTime;

    //TODO  Add stuff for Maximum favorable excursion and Maximum adverse excursion, see https://github.com/kumlien/fx2/issues/27

    //Used by spring data
    @PersistenceConstructor
    Trade(String id, TradeStatus status, String depotId, String robotId, Broker broker, Long brokerId, BigDecimal units, OrderSide side, CurrencyPair instrument, Instant openTime, BigDecimal openPrice, BigDecimal takeProfit, BigDecimal stopLoss, BigDecimal trailingStop, BigDecimal trailingAmount, BigDecimal closePrice, BigDecimal gainPerUnit, BigDecimal totalGain, Instant closeTime) {
        this.id = id;
        this.depotId = depotId;
        this.status = status;
        this.robotId = robotId;
        this.broker = broker;
        this.brokerId = brokerId;
        this.units = units;
        this.side = side;
        this.instrument = instrument;
        this.openTime = openTime;
        this.openPrice = openPrice;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.trailingStop = trailingStop;
        this.trailingAmount = trailingAmount;
        this.closePrice = closePrice;
        this.gainPerUnit = gainPerUnit;
        this.totalGain = totalGain;
        this.closeTime = closeTime;
    }

    /**
     * Create a new trade.
     *
     * @param depotId The id of the fx2 depot
     * @param robotId
     * @param broker
     * @param brokerId The broker id for this trade
     * @param units
     * @param side
     * @param instrument
     * @param openTime
     * @param openPrice
     * @param takeProfit
     * @param stopLoss
     * @param trailingStop
     */
    public Trade(String depotId, String robotId, Broker broker, Long brokerId, BigDecimal units, OrderSide side, CurrencyPair instrument, Instant openTime, BigDecimal openPrice, BigDecimal takeProfit, BigDecimal stopLoss, BigDecimal trailingStop) {
        this(null, TradeStatus.OPEN, depotId, robotId, broker, brokerId, units, side, instrument, openTime, openPrice, takeProfit, stopLoss, trailingStop, null, null, null, null, null);
    }

    public String getId() {
        return id;
    }

    public TradeStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("status", status)
                .add("depotId", depotId)
                .add("robotId", robotId)
                .add("broker", broker)
                .add("brokerId", brokerId)
                .add("units", units)
                .add("side", side)
                .add("instrument", instrument)
                .add("openTime", openTime)
                .add("openPrice", openPrice)
                .add("takeProfit", takeProfit)
                .add("stopLoss", stopLoss)
                .add("trailingStop", trailingStop)
                .add("trailingAmount", trailingAmount)
                .add("closePrice", closePrice)
                .add("gainPerUnit", gainPerUnit)
                .add("totalGain", totalGain)
                .add("closeTime", closeTime)
                .toString();
    }


}
