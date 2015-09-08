package hoggaster.talib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

/**
 * Representation of a {@link TALibService#rsi(double[], int)} calculation.
 */
public class RSIResult {
    
    public final RetCode returnCode;
    public final List<Double> rsiValues;
    public final int beginIndex;

    public RSIResult(RetCode returnCode, double[] out, MInteger outBegIndex, MInteger outNBElement) {
	Preconditions.checkArgument(returnCode != null);
	Preconditions.checkArgument(out != null);
	Preconditions.checkArgument(out.length > 0);
	Preconditions.checkArgument(outBegIndex != null && outBegIndex.value >= 0);
	Preconditions.checkArgument(outNBElement != null && outNBElement.value == out.length);
	this.returnCode = returnCode;
	List<Double> temp = new ArrayList<>();
	for(int i=0; i<out.length; i++) {
	    temp.add(out[i]);
	}
	rsiValues = Collections.unmodifiableList(temp);
	this.beginIndex = outBegIndex.value;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("RSIResult [returnCode=").append(returnCode).append(", rsiValues=").append(rsiValues).append(", beginIndex=").append(beginIndex).append("]");
	return builder.toString();
    }
}
