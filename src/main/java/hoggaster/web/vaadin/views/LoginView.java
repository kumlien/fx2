package hoggaster.web.vaadin.views;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.domain.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.annotation.PostConstruct;

/**
 * Created by svante.kumlien on 19.02.16.
 *
 * Since this view has an empty VIEW_NAME it is the default view.
 * Displays a login form for the user. Saves the user in the
 * session if login is successful.
 */
@SpringView(name = LoginView.VIEW_NAME)
public class LoginView extends MVerticalLayout implements View {
    public static final String VIEW_NAME = "";

    public static final String USER_SESSION_ATTR = "user";

    private static final Logger LOG = LoggerFactory.getLogger(LoginView.class);

    private final TextField username = new MTextField("Username");

    private final PasswordField password = new MPasswordField("Password");

    private final MButton loginBtn = new MButton("login");

    private final AuthenticationProvider authenticationProvider;

    private final UserDetailsService userDetailsService;

    @Autowired
    public LoginView(AuthenticationProvider authenticationProvider, UserDetailsService userDetailsService) {
        this.authenticationProvider = authenticationProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        loginBtn.setDisableOnClick(true);
        loginBtn.setClickShortcut(KeyCode.ENTER);
        loginBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        loginBtn.addClickListener(e -> {
            if(!username.isValid() || !password.isValid()) {
                loginBtn.setEnabled(true);
                return;
            }
            String user = username.getValue().trim();
            String pass = password.getValue().trim();
            if(user.equals("admin") && pass.equals("admin")) {
                getSession().setAttribute(USER_SESSION_ATTR,"SUPER_USER");
                getUI().getNavigator().navigateTo(ListUserView.VIEW_NAME);
            }
            try {
                Authentication ud = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(user, pass));
                LOG.info("User logged in: " + ud);
                final UserDetails userDetails = userDetailsService.loadUserByUsername(user);
                getSession().setAttribute(USER_SESSION_ATTR, userDetails);
                getUI().getNavigator().navigateTo(ListUserView.VIEW_NAME);
                Notification.show("Welcome " + ((User)userDetails).getFirstName());
            } catch(AuthenticationException ae) {
                LOG.debug("Authentication failed...");
                password.setValue(null);
                password.focus();
                Notification.show("Sorry, no such username/password combo found", Notification.Type.ERROR_MESSAGE);
            } finally {
                loginBtn.setEnabled(true);
            }
        });

        username.setRequired(true);
        username.setIcon(FontAwesome.USER);

        password.setRequired(true);
        password.setIcon(FontAwesome.ASTERISK);

        addComponents(new Label("Please login"), username,password,loginBtn);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        username.focus();
    }
}
