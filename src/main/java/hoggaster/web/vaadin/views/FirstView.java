package hoggaster.web.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
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
@SpringView(name = FirstView.VIEW_NAME)
public class FirstView extends MVerticalLayout implements View {
    public static final String VIEW_NAME = "";

    private static final Logger LOG = LoggerFactory.getLogger(FirstView.class);

    private final TextField username = new MTextField("Username");

    private final PasswordField password = new MPasswordField("Password");

    private final MButton loginBtn = new MButton("login");

    private final AuthenticationProvider authenticationProvider;

    private final UserDetailsService userDetailsService;

    @Autowired
    public FirstView(AuthenticationProvider authenticationProvider, UserDetailsService userDetailsService) {
        this.authenticationProvider = authenticationProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        /*
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
        */
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

}
