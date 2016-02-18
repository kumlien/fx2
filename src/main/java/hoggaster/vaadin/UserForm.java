package hoggaster.vaadin;

import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import hoggaster.domain.users.User;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by svante2 on 2016-02-14.
 */
public class UserForm extends AbstractForm<User> {

    TextField firstName = new MTextField("First name");
    TextField email = new MTextField("Email");
    TextField lastName = new MTextField("Last name");
    TextField username = new MTextField("Username");
    PasswordField password = new MPasswordField("Password");
    PasswordField password2 = new MPasswordField("Password (repeated)");

    public UserForm(User user) {
        setSizeUndefined();
        setEntity(user);
    }

    @Override
    protected Component createContent() {
        return new MVerticalLayout(
                new MFormLayout(
                        firstName,
                        lastName,
                        username,
                        email,
                        password,
                        password2
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
    }
}
