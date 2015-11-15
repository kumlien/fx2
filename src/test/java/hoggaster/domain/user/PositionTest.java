package hoggaster.domain.user;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depot.Position;
import hoggaster.domain.orders.OrderSide;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;


public class PositionTest {

    Position position;

    CurrencyPair currencyPair = CurrencyPair.AUD_USD;

    @Before
    public void init() {
        position = new Position(currencyPair, OrderSide.buy);
    }

    @Test
    public void testAddOnce() {
        position.add(BigDecimal.TEN, BigDecimal.ONE);
        Assert.assertTrue(BigDecimal.TEN.compareTo(position.getQuantity()) == 0);
        Assert.assertTrue(BigDecimal.ONE.compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test
    public void testAddTwice() {
        position.add(BigDecimal.TEN, new BigDecimal(25.0));
        position.add(new BigDecimal(20.0), new BigDecimal(35.0));
        BigDecimal average = new BigDecimal(950).divide(new BigDecimal(30), MathContext.DECIMAL64);
        Assert.assertTrue(new BigDecimal("30").compareTo(position.getQuantity()) == 0) ;
        Assert.assertTrue(average.compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractOnly() {
        position.subtract(new BigDecimal(10.0));
    }

    @Test
    public void testAddThenSubtract() {
        position.add(new BigDecimal(1.5), new BigDecimal(100.0));
        position.subtract(new BigDecimal(1.2));
        Assert.assertTrue(new BigDecimal("0.3").compareTo(position.getQuantity()) == 0);
    }
}
