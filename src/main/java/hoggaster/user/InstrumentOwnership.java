package hoggaster.user;

import hoggaster.domain.Instrument;

import java.math.BigDecimal;

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

    public void add(BigDecimal incomingQuantity, BigDecimal incomingPricePerShare) {
	Preconditions.checkArgument(incomingQuantity != null, "Quantity can't be null");
	Preconditions.checkArgument(incomingPricePerShare != null, "Price per share can't be null");
	Preconditions.checkArgument(incomingPricePerShare.intValue() > 0, "Price per share must be a positive value (provided value: " + incomingPricePerShare + ")");
	LOG.info("Adding {} units of {} with price per share {}, averagePPS before adding is {}", incomingQuantity, instrument, incomingPricePerShare, getAveragePricePerShare());
	synchronized (this) {
	    BigDecimal oldTotalValue = this.quantity.multiply(averagePricePerShare);
	    BigDecimal incomingTotalValue = incomingQuantity.multiply(incomingPricePerShare);

	    BigDecimal newTotalQty = this.quantity.add(incomingQuantity);
	    BigDecimal newTotalValue = oldTotalValue.add(incomingTotalValue);
	    this.averagePricePerShare = newTotalValue.divide(newTotalQty);
	    this.quantity = newTotalQty;
	}
	LOG.info("After adding, the quantity is {} and averagePPS is {}", this.quantity, getAveragePricePerShare());
    }

    public BigDecimal getAveragePricePerShare() {
	return averagePricePerShare;
    }
}
