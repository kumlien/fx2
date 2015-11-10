package hoggaster.depot;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import org.slf4j.Logger;
import org.springframework.data.annotation.PersistenceConstructor;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Represents the ownership of an currencyPair
 */
public class Position {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Position.class);

    public static MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public final CurrencyPair currencyPair;

    private BigDecimal quantity;

    private BigDecimal averagePricePerShare;

    public Position(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        quantity = new BigDecimal(0);
        averagePricePerShare = new BigDecimal(0l);
    }

    @PersistenceConstructor
    Position(CurrencyPair currencyPair, BigDecimal quantity, BigDecimal averagePricePerShare) {
        this.currencyPair = currencyPair;
        this.quantity = quantity;
        this.averagePricePerShare = averagePricePerShare;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public Double getQuantity() {
        return quantity.doubleValue();
    }


    /**
     * Add a number of shares with the specified price per share.
     *
     * @param incomingQuantity
     * @param incomingPPS
     */
    public Double add(BigDecimal incomingQuantity, BigDecimal incomingPPS) {
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
        return quantity.doubleValue();
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

    public Double getAveragePricePerShare() {
        return averagePricePerShare.doubleValue();
    }
}
