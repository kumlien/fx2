package hoggaster.rules;

import org.junit.Assert;
import org.junit.Test;

public class OperationsTest {

	@Test
	public void testGreaterThan() {
		Assert.assertTrue(Comparator.GREATER_THAN.apply(2.0, 1.0));
		Assert.assertFalse(Comparator.GREATER_THAN.apply(1.0, 1.0));
		Assert.assertFalse(Comparator.GREATER_THAN.apply(0.5, 1.0));
	}
	
	@Test
	public void testGreaterOrEqualTo() {
		Assert.assertTrue(Comparator.GREATER_OR_EQUAL_THAN.apply(2.0, 1.0));
		Assert.assertTrue(Comparator.GREATER_OR_EQUAL_THAN.apply(1.0, 1.0));
		Assert.assertFalse(Comparator.GREATER_OR_EQUAL_THAN.apply(0.1, 1.0));
	}
	
	@Test
	public void testLessThan() {
		Assert.assertFalse(Comparator.LESS_THAN.apply(2.0, 1.0));
		Assert.assertFalse(Comparator.LESS_THAN.apply(1.0, 1.0));
		Assert.assertTrue(Comparator.LESS_THAN.apply(0.5, 1.0));
	}
	
	@Test
	public void testLessOrEqualTo() {
		Assert.assertFalse(Comparator.LESS_OR_EQUAL_THAN.apply(2.0, 1.0));
		Assert.assertTrue(Comparator.LESS_OR_EQUAL_THAN.apply(1.0, 1.0));
		Assert.assertTrue(Comparator.LESS_OR_EQUAL_THAN.apply(0.1, 1.0));
	}
	
}
