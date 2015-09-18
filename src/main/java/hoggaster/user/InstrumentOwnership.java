package hoggaster.user;

import com.google.common.base.Preconditions;
import hoggaster.domain.Instrument;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Represents the ownership of an instrument
 */
public class InstrumentOwnership {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(InstrumentOwnership.class);

    public static MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public final Instrument instrument;

    private BigDecimal quantity;

    private BigDecimal averagePricePerShare;

    public InstrumentOwnership(Instrument instrument) {
        this.instrument = instrument;
        quantity = new BigDecimal(0);
        averagePricePerShare = new BigDecimal(0l);
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Double getQuantity() {
        return quantity.doubleValue();
    }


    /**
     * Add a number of shares with the specified price per share.
     *
     * @param incomingQuantity
     * @param incomingPricePerShare
     */
    public Double add(BigDecimal incomingQuantity, BigDecimal incomingPPS) {
        Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
        Preconditions.checkArgument(incomingQuantity.doubleValue() > 0, "Quantity must be > 0");
        Preconditions.checkArgument(incomingPPS != null, "Price per share can't be null");
        Preconditions.checkArgument(incomingPPS.doubleValue() > 0, "Price per share must be a positive value (provided value: " + incomingPPS + ")");
        LOG.info("Adding {} units of {} with price per share {}, averagePPS before adding is {}", incomingQuantity, instrument, incomingPPS, getAveragePricePerShare());
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
        LOG.info("Subtracting {} units of {}. Before subtracting we own {} units.", incomingQuantity, instrument, this.quantity);
        synchronized (this) {
            this.quantity = this.quantity.subtract(incomingQuantity, MATH_CONTEXT);
        }
        LOG.info("After subtracting {} we now have {} of {}", incomingQuantity, quantity, instrument);
        return this.quantity;
    }

    public Double getAveragePricePerShare() {
        return averagePricePerShare.doubleValue();
    }
}
