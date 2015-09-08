package hoggaster.talib;

import com.tictactec.ta.lib.Core;

/**
 * Our wrapper around the TA-Lib {@link Core} class.
 */
public interface TALibService {

    /**
     * Calculate the rsi based on the specified values for the specified time period.
     * 
     * @param values The values used to calculate the rsi
     * @param periods The number of periods to use in the calculation
     * @return A {@link RSIResult}
     */
    RSIResult rsi(double[] values, int periods);

}
