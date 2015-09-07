package hoggaster.talib;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class RSIResult {
    
    public final RetCode returnCode;
    public final List<Double> rsiValues = new ArrayList<>();
    public final int beginIndex;

    public RSIResult(RetCode returnCode, double[] out, MInteger outBegIndex, MInteger outNBElement) {
	Preconditions.checkArgument(returnCode != null);
	Preconditions.checkArgument(out != null);
	Preconditions.checkArgument(out.length > 0);
	Preconditions.checkArgument(outBegIndex != null && outBegIndex.value >= 0);
	Preconditions.checkArgument(outNBElement != null && outNBElement.value == out.length);
	this.returnCode = returnCode;
	for(int i=0; i<out.length; i++) {
	    rsiValues.add(out[i]);
	}
	this.beginIndex = outBegIndex.value;
    }
}
