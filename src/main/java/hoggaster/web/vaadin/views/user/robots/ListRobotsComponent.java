package hoggaster.web.vaadin.views.user.robots;

import com.vaadin.event.Action;
import com.vaadin.spring.annotation.ViewScope;
import hoggaster.domain.robot.Robot;
import hoggaster.domain.robot.RobotRegistry;
import hoggaster.robot.RobotDefinitionRepo;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.Serializable;
import java.util.Collection;

/**
 * Used to display a list of all robots for a user.
 * <p>
 * <p>
 * Created by svante.kumlien on 15.04.16.
 */
@Component
@ViewScope
public class ListRobotsComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListRobotsComponent.class);

    private static final Action STOP_ROBOT_ACTION = new Action("Stop this robot");

    private static final Action START_ROBOT_ACTION = new Action("Start this robot");

    private static final Action EDIT_ROBOT_ACTION = new Action("Edit this robot");

    private final RobotDefinitionRepo robotDefinitionRepo;

    private final RobotRegistry robotRegistry;

    private MTable<UIRobot> robotsTable;

    private FormUser user;

    private Collection<Robot> runningRobots;

    @Autowired
    public ListRobotsComponent(RobotDefinitionRepo robotDefinitionRepo, RobotRegistry robotRegistry) {
        this.robotDefinitionRepo = robotDefinitionRepo;
        this.robotRegistry = robotRegistry;
    }

    //Create the tab with the robot defintions and the running robots
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;

        MVerticalLayout tab = new MVerticalLayout();
        robotsTable = new MTable<>(UIRobot.class).withFullWidth();

        populateTableFromDb();
        tab.addComponents(robotsTable);
        tab.expand(robotsTable);
        return tab;
    }

    //Used to clean up the eventbus registrations we create
    public void deregisterAll() {
    }

    private final void deregister(UIRobot robot) {
    }



    //Read all depots from db and their open trades.
    private void populateTableFromDb() {
        //robotDefinitionRepo.findByUserId(user.getId()).stream().map(UIRobot::new).peek(robotsTable::addBeans);
    }
}
