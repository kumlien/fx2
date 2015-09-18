package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;
import hoggaster.prices.Price;
import hoggaster.robot.RobotExecutionContext;

import static hoggaster.rules.MarketUpdateType.PRICE;

/**
 * Indicator used to get the current ask value.
 * Works on a {@link Price} update.
 *
 * @author svante2
 */
public class CurrentAskIndicator implements Indicator {

    @Override
    public Double value(RobotExecutionContext ctx) {
        Preconditions.checkArgument(ctx != null);
        Preconditions.checkArgument(ctx.marketUpdate != null);
        Preconditions.checkArgument(ctx.marketUpdate.getType() == PRICE, "This indicator can only handle Price updates, not " + ctx.marketUpdate.getType());
        return ((Price) ctx.marketUpdate).ask;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "CurrentAskIndicator";
    }

}
