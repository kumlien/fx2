package hoggaster.rules.conditions;

import hoggaster.rules.indicators.CompoundIndicator;
import hoggaster.rules.indicators.Indicator;
import hoggaster.rules.indicators.SimpleValueIndicator;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;
import org.junit.Test;

//TODO Add more tests...
public class CompoundIndicatorTest {
    
    @Test
    public void testAddTwoSimple() {
	Indicator i1 = new SimpleValueIndicator(10.0);
	Indicator i2 = new SimpleValueIndicator(10.0);
	CompoundIndicator ci = new CompoundIndicator(i1, i2, CompoundIndicator.Operator.ADD);
	Double value = ci.value(null);
	Assert.assertTrue(value == 20.0);
    }
    
    @Test
    public void testAddTwoDecimal() {
	Indicator i1 = new SimpleValueIndicator(10.4511111);
	Indicator i2 = new SimpleValueIndicator(10.4922222);
	CompoundIndicator ci = new CompoundIndicator(i1, i2, CompoundIndicator.Operator.ADD);
	Double value = ci.value(null);
	value = new BigDecimal(value, MathContext.DECIMAL32).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	Assert.assertTrue(value == 20.94);
    }
}
