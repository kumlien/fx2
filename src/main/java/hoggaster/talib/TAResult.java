package hoggaster.talib;

import com.google.common.base.Preconditions;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a {@link TALibService#rsi(double[], int)} calculation.
 */
public class TAResult {

    public final RetCode returnCode;
    public final List<Double> values;
    public final int beginIndex;

    public TAResult(RetCode returnCode, double[] resultValues, int outBegIndex, int outNBElement) {
        Preconditions.checkArgument(returnCode != null);
        Preconditions.checkArgument(resultValues != null);
        Preconditions.checkArgument(resultValues.length > 0);
        Preconditions.checkArgument(outBegIndex >= 0);
        Preconditions.checkArgument(outNBElement == resultValues.length, "The specified number of elements doesn't match the actual number: " + outNBElement + " Vs " + resultValues.length);
        this.returnCode = returnCode;

        List<Double> temp = new ArrayList<>();
        for (int i = 0; i < resultValues.length; i++) {
            temp.add(resultValues[i]);
        }
        values = Collections.unmodifiableList(temp);
        this.beginIndex = outBegIndex;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RSIResult [returnCode=").append(returnCode).append(", values=").append(values).append(", beginIndex=").append(beginIndex).append("]");
        return builder.toString();
    }
}
