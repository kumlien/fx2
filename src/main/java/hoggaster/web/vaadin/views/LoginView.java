package hoggaster.web.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by svante.kumlien on 19.02.16.
 */
@SpringView(name = LoginView.VIEW_NAME)
public class LoginView extends MVerticalLayout implements View {
    public static final String VIEW_NAME = "";

    private final TextField username = new MTextField("Username");

    private final PasswordField password = new MPasswordField("Password");

    private final Button loginBtn = new MButton("Login");

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        loginBtn.addClickListener(e -> {
            if(username.isValid() && password.isValid()) {
                System.out.println("hej!");
            }
        });

        username.setRequired(true);
        username.setIcon(FontAwesome.USER);

        password.setRequired(true);
        password.setIcon(FontAwesome.ASTERISK);

        username.focus();
        addComponents(new Label("Please login"), username,password,loginBtn);

        
    }
}
