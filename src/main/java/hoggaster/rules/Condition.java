package hoggaster.rules;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.conditions.Side;

/**
 * Interface for a condition. All conditions must be annotated with @Rule in
 * order to work with our rule engine.
 * <p>
 * Each condition is of a {@link Side}
 * Each condition react to one or more {@link MarketUpdateType}s TODO how to enforce that on the interface level
 */
public interface Condition {

    /**
     * Set the handle to the current evaluation context
     *
     * @param ctx
     */
    void setContext(RobotExecutionContext ctx);

}
