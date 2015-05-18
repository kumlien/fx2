package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

public class SimpleValueIndicator implements Indicator {
	
	public final Double value;

	public SimpleValueIndicator(Double value) {
		Preconditions.checkNotNull(value);
		this.value = value;
	}
	
	@Override
	public Double value(RobotExecutionContext ctx) {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleValueIndicator [value=").append(value)
				.append("]");
		return builder.toString();
	}
}
