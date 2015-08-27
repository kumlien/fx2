package hoggaster.rules.indicators;

import static hoggaster.rules.EventType.PRICE;
import hoggaster.prices.Price;
import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.Indicator;

import com.google.common.base.Preconditions;

public class CurrentAskIndicator implements Indicator {

	@Override
	public Double value(RobotExecutionContext ctx) {
	    Preconditions.checkArgument(ctx != null);
	    Preconditions.checkArgument(ctx.marketUpdate != null);
	    Preconditions.checkArgument(ctx.eventType == PRICE, "This indicator can only handle Price updates, not " + ctx.eventType);
	    return ((Price)ctx.marketUpdate).ask;
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "CurrentAskIndicator";
	}

}
