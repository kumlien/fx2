package hoggaster.web.vaadin.views.user.robots;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by svante2 on 2016-09-09.
 */
public class RobotForm extends AbstractForm<UIRobot> {

    private TextField name = new MTextField("name").withCaption("The name of the robot").withRequired(true).withRequiredError("You must provide a name for the robot");

    public RobotForm () {
        setEntity(new UIRobot());
    }

    @Override
    protected Component createContent() {
        return new MVerticalLayout(
                new MFormLayout(name).withWidth(""),
                getToolbar()
        ).withWidth("");
    }
}
