package hoggaster.talib;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public double rsi(double[] values, int timePeriod) {
	
	LOG.info("About to calc rsi based on values: {}", Arrays.toString(values));
	double[] out = new double[values.length];
	MInteger outBegIndex = new MInteger();
	MInteger outNBElement = new MInteger();
	RetCode returnCode = lib.rsi(5, values.length - 1, values, timePeriod, outBegIndex, outNBElement, out);
	if(returnCode != RetCode.Success) {
	    throw new RuntimeException("Unable to calculate RSI due to " + returnCode);
	}
	LOG.info("Begin index: {}", outBegIndex.value);
	LOG.info("Out NB Element: {}", outNBElement.value);
	LOG.info("Result: {}", Arrays.toString(out));
	LOG.info("RSI returned: {}", out[outNBElement.value -1]);
	
	StringBuilder sb = new StringBuilder("\n");
	for(int i=0; i<values.length; i++ ) {
	    sb.append("Index ").append(i).append(" value: ").append(values[i]).append(", rsi: ");
	    if(i>outBegIndex.value) {
		sb.append(out[i-outBegIndex.value]);
	    } else {
		sb.append("-");
	    }
	    sb.append("\n");
	}
	LOG.info(sb.toString());
	return out[outNBElement.value -1];
    }
}
