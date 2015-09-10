package hoggaster.talib;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

@Service
public class TALibServiceImpl implements TALibService {

    private static final Logger LOG = LoggerFactory.getLogger(TALibServiceImpl.class);

    private final Core lib;

    public TALibServiceImpl() {
	this.lib = new Core();
    }

    @Override
    public TAResult rsi(double[] values, int periods) {
	Preconditions.checkArgument(values != null);
	Preconditions.checkArgument(periods >= 2, "The number of periods must be > 2 but is " + periods); // min periods
	Preconditions.checkArgument(periods < 100000, "The number of periods must be < 100.000"); // max periods
	Preconditions.checkArgument(values.length > periods, "The number of values provided is less than the number of periods specified (" + values.length + " vs " + periods + ")");
	for (int i = 0; i < values.length; i++) {
	    if (values[i] < 0) {
		throw new IllegalArgumentException("Value at index " + i + " is negative (" + values[i] + ")");
	    }
	}

	LOG.info("About to calc rsi based on values: {}", Arrays.toString(values));
	double[] out = new double[values.length];
	MInteger outBegIndex = new MInteger();
	MInteger outNBElement = new MInteger();
	RetCode returnCode = lib.rsi(0, values.length - 1, values, periods, outBegIndex, outNBElement, out);
	if (returnCode != RetCode.Success) {
	    throw new RuntimeException("Unable to calculate RSI due to " + returnCode);
	}
	double[] cleanOut = new double[outNBElement.value];
	System.arraycopy(out, 0, cleanOut, 0, outNBElement.value);
	return new TAResult(returnCode, cleanOut, outBegIndex.value, outNBElement.value);
    }

    @Override
    public TAResult rsi(List<Double> values, int periods) {
	double[] dValues = toArray(values);
	return rsi(dValues, periods);
    }

    @Override
    public TAResult sma(double[] values, int periods) {
	double[] result = new double[values.length];
	MInteger outBegIdx = new MInteger();
	MInteger outNBElement = new MInteger();
	RetCode returnCode = lib.sma(0, values.length - 1, values, periods, outBegIdx, outNBElement, result);
	if (returnCode != RetCode.Success) {
	    throw new RuntimeException("Unable to calculate SMA due to " + returnCode);
	}
	double[] cleanOut = new double[outNBElement.value];
	System.arraycopy(result, 0, cleanOut, 0, outNBElement.value);
	return new TAResult(returnCode, cleanOut, outBegIdx.value, outNBElement.value);
    }

    @Override
    public TAResult sma(List<Double> values, int periods) {
	double[] dValues = toArray(values);
	return sma(dValues, periods);
    }

    private static final double[] toArray(List<Double> values) {
	double[] dValues = toArray(values);
	for (int i = 0; i < dValues.length; i++) {
	    dValues[i] = values.get(i);
	}
	return dValues;
    }

}
