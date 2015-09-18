package hoggaster.user;

import hoggaster.domain.Instrument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;


public class InstrumentOwnershipTest {

    InstrumentOwnership ownership;

    Instrument instrument = Instrument.AUD_CAD;

    @Before
    public void init() {
        ownership = new InstrumentOwnership(instrument);
    }

    @Test
    public void testAddOnce() {
        ownership.add(new BigDecimal(10.0), new BigDecimal(25.0));
        Assert.assertTrue(10.0 == ownership.getQuantity());
        Assert.assertTrue(25.0 == ownership.getAveragePricePerShare());
    }

    @Test
    public void testAddTwice() {
        ownership.add(new BigDecimal(10.0), new BigDecimal(25.0));
        ownership.add(new BigDecimal(20.0), new BigDecimal(35.0));
        BigDecimal average = new BigDecimal(950).divide(new BigDecimal(30), MathContext.DECIMAL64);
        Assert.assertTrue(30.0 == ownership.getQuantity());
        Assert.assertTrue(average.doubleValue() == ownership.getAveragePricePerShare());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractOnly() {
        ownership.subtract(new BigDecimal(10.0));
    }

    @Test
    public void testAddThenSubtract() {
        ownership.add(new BigDecimal(1.5), new BigDecimal(100.0));
        ownership.subtract(new BigDecimal(1.2));
        Assert.assertTrue(0.3 == ownership.getQuantity());
    }
}
