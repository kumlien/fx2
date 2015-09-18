package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;
import hoggaster.prices.Price;
import hoggaster.robot.RobotExecutionContext;

/**
 * Indicator used to get the current bid value.
 * Works on a {@link Price} update
 *
 * @author svante2
 */
public class CurrentBidIndicator implements Indicator {

    @Override
    public Double value(RobotExecutionContext ctx) {
        Preconditions.checkArgument(ctx != null);
        Preconditions.checkArgument(ctx.marketUpdate != null);
        Preconditions.checkArgument(ctx.marketUpdate instanceof Price, "This indicator can only handle Price updates, not " + ctx.marketUpdate.getClass().getSimpleName());
        return ((Price) ctx.marketUpdate).bid;
    }

    @Override
    public String toString() {
        return "CurrentBidIndicator";
    }

}
