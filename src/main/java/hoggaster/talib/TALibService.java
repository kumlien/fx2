package hoggaster.talib;


public interface TALibService {

    /**
     * Calculate the rsi based on the specified values for the specified time period.
     * 
     * @param values
     * @param timePeriod
     * @return An array containing the rsi values.
     */
    RSIResult rsi(double[] values, int timePeriod);

}
