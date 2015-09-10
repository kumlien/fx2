package hoggaster.talib;

import java.util.List;

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
     * @return A {@link TAResult}
     */
    TAResult rsi(double[] values, int periods);

    TAResult rsi(List<Double> values, int periods);

    
    /**
     * Calculate a simple moving average
     * 
     * @param values
     * @param periods
     * @return A {@link TAResult}
     */
    TAResult sma(List<Double> values, int periods);
    
    /**
     * Calculate a simple moving average
     * 
     * @param values
     * @param periods
     * @return A {@link TAResult}
     */
    TAResult sma(double[] values, int periods);

}
