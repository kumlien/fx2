package hoggaster.web.vaadin.views.login;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by svante2 on 2016-04-28.
 */
public class LoginForm extends AbstractForm<LoginCredentials> {

    private MTextField username = new MTextField("Username");
    private MPasswordField password = new MPasswordField("Password");

    public LoginForm() {
        setEntity(new LoginCredentials());
        username.setIcon(FontAwesome.USER);
        password.setIcon(FontAwesome.ASTERISK);
        setModalWindowTitle("Login");

    }

    @Override
    protected Component createContent() {
        focusFirst();
        setSaveCaption("Login");
        return new MVerticalLayout(
                username,
                password,
                getToolbar()
        );
    }
}
