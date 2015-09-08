package hoggaster.talib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TALibServiceTest {

    TALibService service;

    @Before
    public void init() {
	service = new TALibServiceImpl();
    }

    @Test
    public void testRsiNormal() {
	double values[] = new double[] { 199, 197, 195, 194, 194, 194.5, 198.5, 198, 193.5, 193.5, 193, 193, 194, 196, 199.5, 199, 197.5, 198, 197, 198, 196.5, 194, 190.5, 190.5, 190, 190, 189.5, 188, 187, 187, 187.5, 186.5, 187, 186, 186.5, 186, 184.5, 185.5, 185, 190.5, 194.5, 197, 196.5, 196.5, 194, 167.5, 168.5,
		165, 166, 166, };
	RSIResult result = service.rsi(values, 2);
	int rsi = (int) Math.round(result.rsiValues.stream().reduce((a, b) -> b).get());
	assertEquals(20, rsi);
    }

    @Test
    public void testRsi2() {
	double values[] = new double[] { 46.1250, 47.1250, 46.4375, 46.9375, 44.9375, 44.2500, 44.6250, 45.7500, 47.8125, 47.5625, 47.0000, 44.5625, 46.3125, 47.6875, 46.6875, 45.6875, 43.0625, 43.5625, 44.8750, 43.6875 };
	RSIResult rsi = service.rsi(values, 14);
	double expected[] = new double[] { 51.7787, 48.4771, 41.0734, 42.8634, 47.3818, 43.9921 };
	for(int i=0; i< expected.length; i++) {
	    assertTrue(expected[i] + " is  not " + rsi.rsiValues.get(i), Math.round(expected[i]) == Math.round(rsi.rsiValues.get(i).doubleValue()));
	}
    }
    
    
    @Test(expected=RuntimeException.class)
    public void testBadData() {
	service.rsi(new double[]{}, 2);
    }
}