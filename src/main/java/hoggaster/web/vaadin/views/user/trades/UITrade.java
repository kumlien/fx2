package hoggaster.web.vaadin.views.user.trades;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.trades.Trade;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a trade which also keeps a ref to the depot it belongs to. Used in the vaadin table.
 * <p>
 * Created by svante.kumlien on 01.03.16.
 */
public class UITrade {

    DbDepot depot;
    Trade trade;

    UITrade(DbDepot depot, Trade trade) {
        this.depot = depot;
        this.trade = trade;
    }

    public UITrade() {

    }

    public String getBrokerId() {
        return depot.brokerId;
    }

    public String getDepotName() {
        return depot.getName();
    }

    public CurrencyPair getInstrument() {
        return trade.instrument;
    }

    public OrderSide getSide() {
        return trade.side;
    }

    public BigDecimal getUnits() {
        return trade.units;
    }

    public Instant getOpenTime() {
        return trade.getOpenTime();
    }

    public BigDecimal getOpenPrice() {
        return trade.getOpenPrice();
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("depot", depot)
                .add("trade", trade)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UITrade uiTrade = (UITrade) o;
        return Objects.equal(trade.brokerId, uiTrade.trade.brokerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trade.brokerId);
    }
}
