package hoggaster.talib;

import org.junit.Before;
import org.junit.Test;

public class TALibServiceTest {

    TALibService service;

    @Before
    public void init() {
	service = new TALibServiceImpl();
    }

    @Test
    public void testRsi() {
	double values[] = new double[] { 199, 197, 195, 194, 194, 194.5, 198.5, 198, 193.5, 193.5, 193, 193, 194, 196, 199.5, 199, 197.5, 198, 197, 198, 196.5, 194, 190.5, 190.5, 190, 190, 189.5, 188, 187, 187, 187.5, 186.5, 187, 186, 186.5, 186, 184.5, 185.5, 185, 190.5, 194.5, 197, 196.5, 196.5, 194, 167.5, 168.5,
		165, 166, 166, };
	double rsi = service.rsi(values, 2);
    }
}
