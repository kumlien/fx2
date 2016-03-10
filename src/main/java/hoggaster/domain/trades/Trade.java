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

import static hoggaster.domain.trades.TradeStatus.OPEN;

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
        this(null, OPEN, depotId, robotId, broker, brokerId, units, side, instrument, openTime, openPrice, takeProfit, stopLoss, trailingStop, null, null, null, null, null);
    }

    public Trade(String depotId, String robotId, Broker broker, Long brokerId, BigDecimal units, OrderSide side, CurrencyPair instrument, Instant openTime, BigDecimal openPrice, BigDecimal takeProfit, BigDecimal stopLoss, BigDecimal trailingStop, BigDecimal trailingAmount, BigDecimal closePrice, BigDecimal gainPerUnit, BigDecimal totalGain, Instant closeTime) {
        this(null, OPEN, depotId, robotId, broker, brokerId, units, side, instrument, openTime, openPrice, takeProfit, stopLoss, trailingStop, trailingAmount, closePrice, gainPerUnit, totalGain, closeTime);
    }

    public Trade(TradeStatus status, String depotId, String robotId, Broker broker, Long brokerId, BigDecimal units, OrderSide side, CurrencyPair instrument, Instant openTime, BigDecimal openPrice, BigDecimal takeProfit, BigDecimal stopLoss, BigDecimal trailingStop, BigDecimal trailingAmount, BigDecimal closePrice, BigDecimal gainPerUnit, BigDecimal totalGain, Instant closeTime) {
        this(null, status, depotId, robotId, broker, brokerId, units, side, instrument, openTime, openPrice, takeProfit, stopLoss, trailingStop, trailingAmount, closePrice, gainPerUnit, totalGain, closeTime);
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

    public String getDepotId() {
        return depotId;
    }

    public String getRobotId() {
        return robotId;
    }

    public Broker getBroker() {
        return broker;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public OrderSide getSide() {
        return side;
    }

    public CurrencyPair getInstrument() {
        return instrument;
    }

    public Instant getOpenTime() {
        return openTime;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public BigDecimal getTrailingStop() {
        return trailingStop;
    }

    public BigDecimal getTrailingAmount() {
        return trailingAmount;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getGainPerUnit() {
        return gainPerUnit;
    }

    public BigDecimal getTotalGain() {
        return totalGain;
    }

    public Instant getCloseTime() {
        return closeTime;
    }

    public static class TradeBuilder {
        public String depotId;
        public String robotId;
        public Broker broker;
        public Long brokerId;
        public BigDecimal units;
        public OrderSide side;
        public CurrencyPair instrument;
        public Instant openTime;
        public BigDecimal openPrice;
        public BigDecimal takeProfit;
        public BigDecimal stopLoss;
        public BigDecimal trailingStop;
        public BigDecimal trailingAmount;
        private TradeStatus status;
        //Below stuff set when trade is closed.
        private BigDecimal closePrice;
        private BigDecimal gainPerUnit;
        private BigDecimal totalGain;
        private Instant closeTime;

        private TradeBuilder() {
        }

        public static TradeBuilder aTrade() {
            return new TradeBuilder();
        }

        public TradeBuilder withStatus(TradeStatus status) {
            this.status = status;
            return this;
        }

        public TradeBuilder withDepotId(String depotId) {
            this.depotId = depotId;
            return this;
        }

        public TradeBuilder withRobotId(String robotId) {
            this.robotId = robotId;
            return this;
        }

        public TradeBuilder withBroker(Broker broker) {
            this.broker = broker;
            return this;
        }

        public TradeBuilder withBrokerId(Long brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public TradeBuilder withUnits(BigDecimal units) {
            this.units = units;
            return this;
        }

        public TradeBuilder withSide(OrderSide side) {
            this.side = side;
            return this;
        }

        public TradeBuilder withInstrument(CurrencyPair instrument) {
            this.instrument = instrument;
            return this;
        }

        public TradeBuilder withOpenTime(Instant openTime) {
            this.openTime = openTime;
            return this;
        }

        public TradeBuilder withOpenPrice(BigDecimal openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        public TradeBuilder withTakeProfit(BigDecimal takeProfit) {
            this.takeProfit = takeProfit;
            return this;
        }

        public TradeBuilder withStopLoss(BigDecimal stopLoss) {
            this.stopLoss = stopLoss;
            return this;
        }

        public TradeBuilder withTrailingStop(BigDecimal trailingStop) {
            this.trailingStop = trailingStop;
            return this;
        }

        public TradeBuilder withTrailingAmount(BigDecimal trailingAmount) {
            this.trailingAmount = trailingAmount;
            return this;
        }

        public TradeBuilder withClosePrice(BigDecimal closePrice) {
            this.closePrice = closePrice;
            return this;
        }

        public TradeBuilder withGainPerUnit(BigDecimal gainPerUnit) {
            this.gainPerUnit = gainPerUnit;
            return this;
        }

        public TradeBuilder withTotalGain(BigDecimal totalGain) {
            this.totalGain = totalGain;
            return this;
        }

        public TradeBuilder withCloseTime(Instant closeTime) {
            this.closeTime = closeTime;
            return this;
        }

        public TradeBuilder but() {
            return aTrade().withStatus(status).withDepotId(depotId).withRobotId(robotId).withBroker(broker).withBrokerId(brokerId).withUnits(units).withSide(side).withInstrument(instrument).withOpenTime(openTime).withOpenPrice(openPrice).withTakeProfit(takeProfit).withStopLoss(stopLoss).withTrailingStop(trailingStop).withTrailingAmount(trailingAmount).withClosePrice(closePrice).withGainPerUnit(gainPerUnit).withTotalGain(totalGain).withCloseTime(closeTime);
        }

        public Trade build() {
            Trade trade = new Trade(status, depotId, robotId, broker, brokerId, units, side, instrument, openTime, openPrice, takeProfit, stopLoss, trailingStop, trailingAmount, closePrice, gainPerUnit, totalGain, closeTime);
            return trade;
        }
    }
}
