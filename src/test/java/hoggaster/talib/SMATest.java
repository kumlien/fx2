package hoggaster.talib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SMATest {

    TALibService service;

    @Before
    public void init() {
        service = new TALibServiceImpl();
    }

    @Test
    public void testSMA() {
        double values[] = {199, 197, 195, 194, 194, 194.5, 198.5, 198, 193.5, 193.5, 193, 193, 194, 196, 199.5, 199, 197.5, 198, 197, 198, 196.5, 194, 190.5, 190.5, 190, 190, 189.5, 188, 187, 187, 187.5, 186.5, 187, 186, 186.5, 186, 184.5, 185.5, 185, 190.5, 194.5, 197, 196.5, 196.5, 194, 167.5, 168.5, 165, 166, 166,};
        double expected[] = {0, 198, 197, 195.3333333, 194.3333333, 194.1666667, 195.6666667, 197, 196.666667, 195, 193.3333333, 193.1666667, 193.3333333, 194.3333333, 196.5, 198.1666667, 198.6666667, 198.1666667, 197.5, 197.6666667, 197.1666667, 196.1666667, 193.6666667, 191.6666667, 190.3333333, 190.1666667,
                189.8333333, 189.1666667, 188.1666667, 187.3333333, 187.1666667, 187, 187, 186.5, 186.5, 186.1666667, 185.6666667, 185.3333333, 185, 187, 190, 194, 196, 196.6666667, 195.6666667, 186, 176.6666667, 167, 166.5, 165.6666667};
        TAResult result = service.sma(values, 3);
        System.out.println("Number of elements: " + result.values.size());
        System.out.println("Start index: " + result.beginIndex);
        for (int i = result.beginIndex; i < result.values.size(); i++) {
            Assert.assertEquals(expected[i], result.values.get(i - result.beginIndex).doubleValue(), 0.1);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testBadData() {
        service.sma(new double[]{}, 2);
    }
}