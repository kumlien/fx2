package hoggaster.domain.depots;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.PersistenceConstructor;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Represents the ownership of an currencyPair
 *
 * @see hoggaster.domain.trades.Trade
 */
public class Position {

    private static final Logger LOG = LoggerFactory.getLogger(Position.class);

    public static MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public final CurrencyPair currencyPair;

    public OrderSide side;

    private BigDecimal quantity;

    private BigDecimal averagePricePerShare;

    /**
     *
     * @param currencyPair
     * @param side
     * @param quantity
     * @param averagePricePerShare
     */
    @PersistenceConstructor
    public Position(CurrencyPair currencyPair, OrderSide side, BigDecimal quantity, BigDecimal averagePricePerShare) {
        LOG.info("New position read from database for {} with order side {}, qty {} and price {}", currencyPair, side, quantity, averagePricePerShare);
        this.currencyPair = currencyPair;
        this.side = side;
        this.quantity = quantity;
        this.averagePricePerShare = averagePricePerShare;
    }

    public Position(Position original) {
        this.side = original.side;
        this.currencyPair = original.getCurrencyPair();
        this.quantity = original.quantity;
        this.averagePricePerShare = original.averagePricePerShare;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }


    /**
     * Add a number of shares with the specified price per share.
     *
     * @param incomingQuantity
     * @param incomingPPS
     * @param side
     */
    public BigDecimal newTrade(BigDecimal incomingQuantity, BigDecimal incomingPPS, OrderSide side) {
        Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
        Preconditions.checkArgument(incomingQuantity.doubleValue() > 0, "Quantity must be > 0");
        Preconditions.checkArgument(incomingPPS != null, "Price per share can't be null");
        Preconditions.checkArgument(incomingPPS.doubleValue() > 0, "Price per share must be a positive value (provided value: " + incomingPPS + ")");
        LOG.info("New {} trade for {} units of {} with price per share {}, averagePPS before adding is {}", side, incomingQuantity, currencyPair, incomingPPS, getAveragePricePerShare());
        synchronized (this) {
            BigDecimal oldTotalValue = quantity.multiply(averagePricePerShare, MATH_CONTEXT);
            BigDecimal incomingTotalValue = incomingQuantity.multiply(incomingPPS, MATH_CONTEXT);

            BigDecimal newTotalQty = this.side == side ? quantity.add(incomingQuantity, MATH_CONTEXT) : quantity.subtract(incomingQuantity, MATH_CONTEXT);
            BigDecimal newTotalValue = this.side == side ? oldTotalValue.add(incomingTotalValue, MATH_CONTEXT) : oldTotalValue.subtract(incomingTotalValue, MATH_CONTEXT);
            this.quantity = newTotalQty;
            if(this.side == side) {
                this.averagePricePerShare = newTotalValue.abs().divide(newTotalQty.abs(), MATH_CONTEXT);
            } else if (quantity.compareTo(BigDecimal.ZERO) < 0) {
                LOG.info("Switching side from {} to {} since the computed quantity is {}", this.side, side, quantity);
                this.side = side;
                quantity = quantity.abs(MATH_CONTEXT);
                averagePricePerShare = incomingPPS;
            } else if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                LOG.info("Position seem to be closed (quantity is zero)");
                averagePricePerShare = BigDecimal.ZERO;
            }
        }
        LOG.info("After new trade, the quantity is {} and averagePPS is {}", this.quantity, getAveragePricePerShare());
        return quantity;
    }


    public BigDecimal getAveragePricePerShare() {
        return averagePricePerShare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return currencyPair == position.currencyPair &&
                side == position.side &&
                Objects.equal(quantity, position.quantity) &&
                Objects.equal(averagePricePerShare, position.averagePricePerShare);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(currencyPair, side, quantity, averagePricePerShare);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("currencyPair", currencyPair)
                .add("side", side)
                .add("quantity", quantity)
                .add("averagePricePerShare", averagePricePerShare)
                .toString();
    }
}
