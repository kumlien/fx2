package hoggaster.rules.conditions;

import hoggaster.rules.indicators.CompoundIndicator;
import hoggaster.rules.indicators.Indicator;
import hoggaster.rules.indicators.SimpleValueIndicator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

//TODO Add more tests...
public class CompoundIndicatorTest {

    @Test
    public void testAddTwoSimple() {
        Indicator i1 = new SimpleValueIndicator(new BigDecimal(10.0));
        Indicator i2 = new SimpleValueIndicator(new BigDecimal(10.0));
        CompoundIndicator ci = new CompoundIndicator(i1, i2, CompoundIndicator.Operator.ADD);
        BigDecimal value = ci.value(null);
        Assert.assertTrue(value.equals(new BigDecimal("20.0")));
    }

    @Test
    public void testAddTwoDecimal() {
        Indicator i1 = new SimpleValueIndicator(new BigDecimal(10.4511111));
        Indicator i2 = new SimpleValueIndicator(new BigDecimal(10.4922222));
        CompoundIndicator ci = new CompoundIndicator(i1, i2, CompoundIndicator.Operator.ADD);
        BigDecimal value = ci.value(null);
        Assert.assertTrue(value.equals(new BigDecimal("20.94")));
    }
}
