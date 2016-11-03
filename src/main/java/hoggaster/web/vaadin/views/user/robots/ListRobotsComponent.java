package hoggaster.web.vaadin.views.user.robots;

import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import hoggaster.domain.depots.DepotRepo;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.robot.RobotService;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import hoggaster.web.vaadin.views.user.robots.rules.RuleDetailsWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;

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

    private static final Action STOP_ROBOT_ACTION = new Action("Stop", FontAwesome.STOP);

    private static final Action START_ROBOT_ACTION = new Action("Start", FontAwesome.ROCKET);

    private static final Action EDIT_ROBOT_ACTION = new Action("Edit", FontAwesome.PENCIL);

    private static final Action ADD_ENTER_TRADE_RULE_ACTION = new Action("Add rule for enter trade", FontAwesome.PENCIL);

    private static final Action ADD_ROBOT_ACTION = new Action("Add a new robot");

    private Button addBtn = new MButton(FontAwesome.PLUS, this::addRobot).withDescription("Add a new robot");

    private Button deleteBtn = new ConfirmButton(FontAwesome.TRASH_O, "Are you sure you want to delete this robot?", this::deleteRobot).withStyleName("danger");

    private Button editBtn = new MButton(FontAwesome.PENCIL_SQUARE_O, this::editRobot);

    private final DepotRepo depotRepo;

    private final RobotService robotService;

    private MTable<UIRobot> robotsTable;

    private List<UIRobot> robotTableModel = new ArrayList<>();

    private FormUser user;

    @Autowired
    public ListRobotsComponent(DepotRepo depotRepo, RobotService robotService) {
        this.depotRepo = depotRepo;
        this.robotService = robotService;
    }

    //Create the tab with the robot definitions
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;

        MVerticalLayout tab = new MVerticalLayout();
        setupTable();

        rePopulateTable();
        HorizontalLayout horizontalLayout = new MHorizontalLayout(addBtn, editBtn, deleteBtn);
        tab.addComponents(horizontalLayout,robotsTable);
        tab.expand(robotsTable);
        return tab;
    }


    private void setupTable() {
        robotsTable = new MTable<>(robotTableModel)
                .withProperties("name", "depotName", "instrument", "orderSide")
                .withColumnHeaders("Name", "Depot", "Instrument", "Side")
                .withGeneratedColumn("Currently running", r -> robotService.getById(r.getId()) != null ? "Yes" : "No")
                .withGeneratedColumn("orderSide", r -> r.getOrderSide() == OrderSide.buy ? "Long" : "Short")
                .setSortableProperties("name", "depotName", "instrument")
                .withFullWidth();
        robotsTable.addMValueChangeListener(e -> adjustButtonState());
        robotsTable.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                UIRobot robot = (UIRobot) target;
                Action[] validActions = null;
                if(robot != null) {
                    if(robotService.isRunning(robot.getId())) {
                        validActions = new Action[]{EDIT_ROBOT_ACTION, STOP_ROBOT_ACTION};
                    } else {
                        validActions = new Action[]{EDIT_ROBOT_ACTION, START_ROBOT_ACTION, ADD_ENTER_TRADE_RULE_ACTION};
                    }
                } else {
                    validActions = new Action[]{ADD_ROBOT_ACTION};
                }
                return validActions;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if(action == ADD_ROBOT_ACTION) {
                    addRobot(null);
                }
                if(action == EDIT_ROBOT_ACTION) {
                    editRobot((UIRobot) target);
                }
                if(action == ADD_ENTER_TRADE_RULE_ACTION) {
                    RuleDetailsWindow window = new RuleDetailsWindow();
                    UI.getCurrent().addWindow(window);
                }
                if(action == START_ROBOT_ACTION) {
                    UIRobot r = (UIRobot) target;
                    robotService.start(r.getRobotDefinition(), r.getDbDepot().getId());
                    rePopulateTable();
                    Notification.show("Started", "The robot is now running!", WARNING_MESSAGE);
                }
                if(action == STOP_ROBOT_ACTION) {
                    robotService.stop(((UIRobot) target).getId());
                    rePopulateTable();
                    Notification.show("Stopped", "The robot is now stopped", WARNING_MESSAGE);
                }
            }
        });

        robotsTable.addRowClickListener(evt -> {
            if(evt.isDoubleClick()) {
                editRobot(evt.getRow());
            }
        });
    }

    private void addRobot(Button.ClickEvent clickEvent) {
        RobotForm form = new RobotForm(depotRepo.findByUserId(user.getId()));

        form.openInModalPopup();
        form.setSavedHandler(robot -> {
            LOG.info("Saving a new robot: {}", robot);
            robot.getDbDepot().addRobotDefinition(new RobotDefinition(robot.getName(), robot.getInstrument(), robot.getOrderSide()));
            depotRepo.save(robot.getDbDepot());
            rePopulateTable();
            form.closePopup();
        });
        form.setResetHandler(entity -> {
            form.closePopup();
        });
    }

    private void deleteRobot(Button.ClickEvent clickEvent) {
        UIRobot robot = robotsTable.getValue();
        LOG.info("Delete robot {}", robot);
        if(robot.getDbDepot().removeRobotDefinition(robot.getId())) {
            depotRepo.save(robot.getDbDepot());
        } else {
            Notification.show("Sorry, unable to delete that robot, please try again", ERROR_MESSAGE);
        }
        robotsTable.setValue(null);
        rePopulateTable();
    }

    private void editRobot(Button.ClickEvent clickEvent) {
        editRobot(robotsTable.getValue());
    }

    private void editRobot(UIRobot robot) {
        if(robotService.isRunning(robot.getId())) {
            Notification.show("Robot is running", "You can only edit a robot which is not currently running", WARNING_MESSAGE);
            return;
        }
        RobotForm form = new RobotForm(robot);
        form.openInModalPopup();
        form.setResetHandler(r -> form.closePopup());
        form.setSavedHandler(r -> {
            r.getDbDepot().updateRobotDefinition(new RobotDefinition(r.getId(), r.getName(), r.getInstrument(), r.getOrderSide(), r.getEnterConditions(), r.getExitConditions()));
            depotRepo.save(r.getDbDepot());
            rePopulateTable();
            form.closePopup();
        });
    }


    //Read all robots from db for the user.
    private void rePopulateTable() {
        Collection<UIRobot> robots = depotRepo.findByUserId(user.getId())
                .stream()
                .map(dbDepot -> dbDepot.getRobotDefinitions().stream().map(robotDef -> new UIRobot(robotDef, dbDepot)).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        robotTableModel.clear();
        robotTableModel.addAll(robots);
        robotsTable.markAsDirtyRecursive();
        adjustButtonState();

    }

    protected void adjustButtonState() {
        boolean hasSelection = robotsTable.getValue() != null;
        editBtn.setEnabled(hasSelection);
        deleteBtn.setEnabled(hasSelection);
    }
}
