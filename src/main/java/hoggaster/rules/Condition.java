package hoggaster.rules;

import hoggaster.robot.RobotExecutionContext;
import hoggaster.rules.conditions.ConditionType;

/**
 * Interface for a condition. All conditions must be annotated with @Rule in
 * order to work with our rule engine.
 * 
 * Each condition is of a {@link ConditionType}
 * Each condition react to one or more {@link EventType}s TODO how to enforce that on the interface level
 */
public interface Condition {

    /**
     * Set the handle to the current evaluation context
     * 
     * @param ctx
     */
    void setContext(RobotExecutionContext ctx);

}
