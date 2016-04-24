package hoggaster.web.vaadin.views.user.robots;

import hoggaster.domain.robot.RobotDefinition;

/**
 * Created by svante2 on 2016-04-24.
 */
public class UIRobot {

    final RobotDefinition robotDefinition;

    public UIRobot(RobotDefinition robotDefinition) {
        this.robotDefinition = robotDefinition;
    }

    public String getName() {
        return robotDefinition.name;
    }
}
