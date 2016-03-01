package hoggaster.web.vaadin.views.user.depots;

import com.google.common.base.MoreObjects;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.positions.Position;

import java.math.BigDecimal;

/**
 * Represents a position
 *
 * Created by svante.kumlien on 01.03.16.
 */
public class UIPosition {

    final DbDepot depot;
    final Position position;

    UIPosition(DbDepot depot, Position position) {
        this.depot = depot;
        this.position = position;
    }

    public String getBrokerDepotId() {
        return depot.brokerId;
    }

    public String getDepotName() {
        return depot.getName();
    }

    public CurrencyPair getCurrencyPair() {
        return position.currencyPair;
    }

    public OrderSide getSide() {
        return position.side;
    }

    public BigDecimal getQuantity() {
        return position.getQuantity();
    }

    public BigDecimal getAveragePricePerShare() {
        return position.getAveragePricePerShare();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("depot", depot)
                .add("position", position)
                .toString();
    }
}
