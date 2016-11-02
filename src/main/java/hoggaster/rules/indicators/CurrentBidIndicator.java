package hoggaster.rules.indicators;

import com.google.common.base.Preconditions;
import hoggaster.domain.prices.Price;
import hoggaster.domain.robot.RobotExecutionContext;

import java.math.BigDecimal;

import static hoggaster.rules.MarketUpdateType.PRICE;

/**
 * Indicator used to get the current bid value.
 * Works on a {@link Price} update
 *
 * @author svante2
 */
public class CurrentBidIndicator implements Indicator {

    @Override
    public BigDecimal value(RobotExecutionContext ctx) {
        Preconditions.checkArgument(ctx != null);
        Preconditions.checkArgument(ctx.marketUpdate != null);
        Preconditions.checkArgument(ctx.marketUpdate.getType() == PRICE, "This indicator can only handle Price updates, not " + ctx.marketUpdate.getClass().getSimpleName());
        return ((Price) ctx.marketUpdate).bid;
    }

    @Override
    public String toString() {
        return "CurrentBidIndicator";
    }

}
