package hoggaster.rules.conditions;

import hoggaster.domain.robot.RobotExecutionContext;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.Rule;

/**
 * Interface for a condition. All conditions must be annotated with @Rule in
 * order to work with our rule engine.
 * <p>
 * Each condition react to one or more {@link MarketUpdateType}s TODO how to enforce that on the interface level
 */
public interface Condition extends Rule {

    /**
     * Set the handle to the current evaluation context
     *
     * @param ctx
     */
    void setContext(RobotExecutionContext ctx);

}
