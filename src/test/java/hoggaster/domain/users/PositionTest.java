package hoggaster.domain.users;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.Position;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static hoggaster.domain.orders.OrderSide.buy;
import static hoggaster.domain.orders.OrderSide.sell;
import static org.junit.Assert.assertTrue;


public class PositionTest {

    Position position;

    CurrencyPair currencyPair = CurrencyPair.AUD_USD;

    BigDecimal initialPrice = BigDecimal.ONE;

    BigDecimal initialQuantity = BigDecimal.TEN;


    @Before
    public void init() {
        position = new Position(currencyPair, buy, initialQuantity, initialPrice);
    }

    @Test
    public void testInitalTrade() {
        assertTrue(BigDecimal.TEN.compareTo(position.getQuantity()) == 0);
        assertTrue("Av. price should be " + BigDecimal.ONE + " but is " + position.getAveragePricePerShare(), BigDecimal.ONE.compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test
    public void testBuyTenSamePrice() {
        position.newTrade(BigDecimal.TEN, BigDecimal.ONE, buy);
        assertTrue(BigDecimal.TEN.add(BigDecimal.TEN).compareTo(position.getQuantity()) == 0);
        assertTrue(BigDecimal.ONE.compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test
    public void testAddTwice() {
        position.newTrade(BigDecimal.TEN, new BigDecimal(25.0), buy);
        position.newTrade(new BigDecimal(20.0), new BigDecimal(35.0), buy);
        BigDecimal average = new BigDecimal(960).divide(new BigDecimal(40), MathContext.DECIMAL64);
        assertTrue(new BigDecimal("40").compareTo(position.getQuantity()) == 0) ;
        assertTrue("Av. price should be " + average + " but is " + position.getAveragePricePerShare(),average.compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test
    public void testBuyThenSell() {
        position.newTrade(new BigDecimal(2), new BigDecimal(40), buy);
        position.newTrade(new BigDecimal(3), new BigDecimal(60), sell);
        assertTrue(new BigDecimal("9").compareTo(position.getQuantity()) == 0);
        assertTrue("Av. price should be 7.5 but is " + position.getAveragePricePerShare(), new BigDecimal("7.5").compareTo(position.getAveragePricePerShare()) == 0);
    }

    @Test
    public void testBuyThenClose() {
        position.newTrade(new BigDecimal(2), new BigDecimal(40), buy);
        position.newTrade(new BigDecimal(12), new BigDecimal(60), sell);
        assertTrue(new BigDecimal("0").compareTo(position.getQuantity()) == 0);
        assertTrue("Av. price should be 0 but is " + position.getAveragePricePerShare(), new BigDecimal("0").compareTo(position.getAveragePricePerShare()) == 0);
    }
}
