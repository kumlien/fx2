package hoggaster.web.vaadin.views.user.robots;

import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import hoggaster.domain.depots.DepotRepo;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.robot.RobotRegistry;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

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

    private static final Action ADD_ROBOT_ACTION = new Action("Add a new robot");

    private Button addNew = new MButton(FontAwesome.PLUS, this::addRobot).withDescription("Add a new robot");

    private Button delete = new MButton(FontAwesome.TRASH, this::deleteRobot).withDescription("Delete");

    private final DepotRepo depotRepo;

    private final RobotRegistry robotRegistry;

    private MTable<UIRobot> robotsTable;

    private FormUser user;

    @Autowired
    public ListRobotsComponent(DepotRepo depotRepo, RobotRegistry robotRegistry) {
        this.depotRepo = depotRepo;
        this.robotRegistry = robotRegistry;
    }

    //Create the tab with the robot definitions
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;

        MVerticalLayout tab = new MVerticalLayout();
        robotsTable = new MTable<>(UIRobot.class)
                .withProperties("name", "depotName", "instrument")
                .withColumnHeaders("Name", "Depot", "Instrument")
                .withGeneratedColumn("Currently running", r -> robotRegistry.getById(r.getId()) != null ? "Yes" : "No")
                .withFullWidth();

        populateTableFromDb();
        HorizontalLayout horizontalLayout = new HorizontalLayout(addNew, delete);
        tab.addComponents(horizontalLayout,robotsTable);
        tab.expand(robotsTable);
        return tab;
    }

    private void addRobot(Button.ClickEvent clickEvent) {
        RobotForm form = new RobotForm(depotRepo.findByUserId(user.getId()));
        Window popup = form.openInModalPopup();
        form.setSavedHandler(robot -> {
            LOG.info("Saving a new robot: {}", robot);
            robot.getDbDepot().addRobotDefinition(new RobotDefinition(robot.getName(), robot.getInstrument()));
            depotRepo.save(robot.getDbDepot());
            popup.close();
            populateTableFromDb();
        });
        form.setResetHandler(entity -> {
            popup.close();
        });
    }

    private void deleteRobot(Button.ClickEvent clickEvent) {
        LOG.info("Delete robot...");
    }

    //Used to clean up the event bus registrations we create
    public void deregisterAll() {
    }

    private final void deregister(UIRobot robot) {
    }

    //Read all robots from db for the user.
    private void populateTableFromDb() {
        Collection<UIRobot> robots = depotRepo.findByUserId(user.getId())
                .stream()
                .map(dbDepot -> dbDepot.getRobotDefinitions().stream().map(robotDef -> new UIRobot(robotDef, dbDepot)).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        robotsTable.addBeans(robots);
    }

}
