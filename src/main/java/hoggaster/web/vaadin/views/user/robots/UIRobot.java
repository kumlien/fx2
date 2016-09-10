package hoggaster.web.vaadin.views.user.robots;

import hoggaster.domain.robot.RobotDefinition;

/**
 * Created by svante2 on 2016-04-24.
 */
public class UIRobot {

    RobotDefinition robotDefinition;

    private String name;

    public UIRobot(RobotDefinition robotDefinition) {
        this.robotDefinition = robotDefinition;
    }

    public UIRobot() {
    }

    public String getName() {
        return robotDefinition != null ? robotDefinition.name : "";
    }

    public void setName(String name) {
        this.name = name;
    }


}
