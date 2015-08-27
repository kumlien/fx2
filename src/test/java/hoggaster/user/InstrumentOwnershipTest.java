package hoggaster.user;

import hoggaster.domain.Instrument;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class InstrumentOwnershipTest {
    
    InstrumentOwnership ownership;
    
    Instrument instrument = Instrument.AUD_CAD;
    
    @Before
    public void init() {
	ownership = new InstrumentOwnership(instrument);
    }
    
    @Test
    public void testAddOnce() {
	BigDecimal qty = new BigDecimal(10.0);
	BigDecimal pps = new BigDecimal(25.0);
	ownership.add(qty, pps);
	Assert.assertEquals(new BigDecimal(10.0), ownership.getQuantity());
	Assert.assertEquals(new BigDecimal(25.0), ownership.getAveragePricePerShare());
    }
    
    @Test
    public void testAddTwice() {
	ownership.add(new BigDecimal(10.0), new BigDecimal(25.0));
	ownership.add(new BigDecimal(20.0), new BigDecimal(35.0));
	BigDecimal average = new BigDecimal(950).divide(new BigDecimal(30), MathContext.DECIMAL64);
	Assert.assertEquals(new BigDecimal(30.0), ownership.getQuantity());
	Assert.assertEquals(average, ownership.getAveragePricePerShare());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSubtractOnly() {
	ownership.subtract(new BigDecimal(10));
    }
    
    public void testAddThenSubtract() {
	//todo
    }
}
