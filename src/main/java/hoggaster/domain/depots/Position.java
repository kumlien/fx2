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
 */
public class Position {

    private static final Logger LOG = LoggerFactory.getLogger(Position.class);

    public static MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public final CurrencyPair currencyPair;

    public final OrderSide side;

    private BigDecimal quantity;

    private BigDecimal averagePricePerShare;

    public Position(CurrencyPair currencyPair, OrderSide side) {
        this.currencyPair = currencyPair;
        this.side = side;
        quantity = new BigDecimal(0);
        averagePricePerShare = new BigDecimal(0l);
    }

    @PersistenceConstructor
    public Position(CurrencyPair currencyPair, OrderSide side, BigDecimal quantity, BigDecimal averagePricePerShare) {
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
     */
    public BigDecimal add(BigDecimal incomingQuantity, BigDecimal incomingPPS) {
        Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
        Preconditions.checkArgument(incomingQuantity.doubleValue() > 0, "Quantity must be > 0");
        Preconditions.checkArgument(incomingPPS != null, "Price per share can't be null");
        Preconditions.checkArgument(incomingPPS.doubleValue() > 0, "Price per share must be a positive value (provided value: " + incomingPPS + ")");
        LOG.info("Adding {} units of {} with price per share {}, averagePPS before adding is {}", incomingQuantity, currencyPair, incomingPPS, getAveragePricePerShare());
        synchronized (this) {
            BigDecimal oldTotalValue = this.quantity.multiply(averagePricePerShare, MATH_CONTEXT);
            BigDecimal incomingTotalValue = incomingQuantity.multiply(incomingPPS, MATH_CONTEXT);

            BigDecimal newTotalQty = this.quantity.add(incomingQuantity, MATH_CONTEXT);
            BigDecimal newTotalValue = oldTotalValue.add(incomingTotalValue, MATH_CONTEXT);
            this.averagePricePerShare = newTotalValue.divide(newTotalQty, MATH_CONTEXT);
            this.quantity = newTotalQty;
        }
        LOG.info("After adding, the quantity is {} and averagePPS is {}", this.quantity, getAveragePricePerShare());
        return quantity;
    }

    /**
     * @param incomingQuantity
     */
    public BigDecimal subtract(BigDecimal incomingQuantity) {
        Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
        Preconditions.checkArgument(incomingQuantity.doubleValue() > 0, "Quantity must be > 0");
        Preconditions.checkArgument(this.quantity.compareTo(incomingQuantity) >= 0, "Not possible to remove " + incomingQuantity + " from " + this.quantity);
        LOG.info("Subtracting {} units of {}. Before subtracting we own {} units.", incomingQuantity, currencyPair, this.quantity);
        synchronized (this) {
            this.quantity = this.quantity.subtract(incomingQuantity, MATH_CONTEXT);
        }
        LOG.info("After subtracting {} we now have {} of {}", incomingQuantity, quantity, currencyPair);
        return this.quantity;
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