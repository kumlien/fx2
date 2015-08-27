package hoggaster.user;

import hoggaster.domain.Instrument;

import java.math.BigDecimal;
import java.math.MathContext;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Represents the ownership of an instrument
 */
public class InstrumentOwnership {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(InstrumentOwnership.class);

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

    public BigDecimal getQuantity() {
	return quantity;
    }

    
    /**
     * Add a number of shares with the specified price per share.
     * @param incomingQuantity
     * @param incomingPricePerShare
     */
    public void add(BigDecimal incomingQuantity, BigDecimal incomingPricePerShare) {
	Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
	Preconditions.checkArgument(incomingQuantity.intValue() > 0, "Quantity must be > 0");
	Preconditions.checkArgument(incomingPricePerShare != null, "Price per share can't be null");
	Preconditions.checkArgument(incomingPricePerShare.intValue() > 0, "Price per share must be a positive value (provided value: " + incomingPricePerShare + ")");
	LOG.info("Adding {} units of {} with price per share {}, averagePPS before adding is {}", incomingQuantity, instrument, incomingPricePerShare, getAveragePricePerShare());
	synchronized (this) {
	    BigDecimal oldTotalValue = this.quantity.multiply(averagePricePerShare);
	    BigDecimal incomingTotalValue = incomingQuantity.multiply(incomingPricePerShare);

	    BigDecimal newTotalQty = this.quantity.add(incomingQuantity);
	    BigDecimal newTotalValue = oldTotalValue.add(incomingTotalValue);
	    this.averagePricePerShare = newTotalValue.divide(newTotalQty, MathContext.DECIMAL64);
	    this.quantity = newTotalQty;
	}
	LOG.info("After adding, the quantity is {} and averagePPS is {}", this.quantity, getAveragePricePerShare());
    }
    
    public void subtract(BigDecimal incomingQuantity) {
	Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
	Preconditions.checkArgument(incomingQuantity.intValue() > 0, "Quantity must be > 0");
	Preconditions.checkArgument(this.quantity.compareTo(incomingQuantity) >= 0, "Not possible to remove " + incomingQuantity + " from " + this.quantity);
	LOG.info("Subtracting {} units of {}. Before subtracting we own {} units.", incomingQuantity, instrument, this.quantity);
	synchronized (this) {
	    this.quantity = this.quantity.subtract(incomingQuantity);
	}
	LOG.info("After subtracting {} we now have {} of {}", incomingQuantity, quantity, instrument);
    }

    public BigDecimal getAveragePricePerShare() {
	return averagePricePerShare;
    }
}
