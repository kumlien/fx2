package hoggaster.web.vaadin;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.domain.users.User;
import hoggaster.web.vaadin.views.FirstView;
import hoggaster.web.vaadin.views.login.LoginCredentials;
import hoggaster.web.vaadin.views.login.LoginForm;
import hoggaster.web.vaadin.views.user.ListUserView;
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
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import static com.vaadin.shared.communication.PushMode.MANUAL;
import static com.vaadin.shared.ui.ui.Transport.WEBSOCKET_XHR;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_BORDERLESS;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_FRIENDLY;

/**
 * http://fortawesome.github.io/Font-Awesome/icons/
 *
 * @author svante2
 */

@Title("FX::2")
@SpringUI(path = "fx2")
@Theme("fx2")
@Push(value = MANUAL, transport = WEBSOCKET_XHR)
public class AdminUI extends UI {

    public static final String USER_SESSION_ATTR = "user";

    public static final Logger LOG = LoggerFactory.getLogger(AdminUI.class);

    //Get the views annotated as SpringView:s
    private final SpringViewProvider viewProvider;

    private final Button loginBtn = new MButton("login", e -> {
        LoginForm form = new LoginForm();
        form.openInModalPopup();
        form.setSavedHandler(u -> login(u, form));
        form.setResetHandler(u -> form.closePopup());
    }).withStyleName(BUTTON_FRIENDLY, BUTTON_BORDERLESS);

    //Button to navigate to the list with users
    private final Button listUsersBtn = createNavigationButton("List users", ListUserView.VIEW_NAME);

    private final AuthenticationProvider authenticationProvider;

    //The logout button, unset the user in the session and go back to login view
    private final Button logoutBtn = new MButton("Logout", e -> {
        getUI().getSession().setAttribute(USER_SESSION_ATTR, null);
        getUI().getNavigator().navigateTo(FirstView.VIEW_NAME);
        loginBtn.setVisible(true);
    });

    private final UserDetailsService userDetailsService;

    @Autowired
    public AdminUI(SpringViewProvider viewProvider, AuthenticationProvider authenticationProvider, UserDetailsService userDetailsService) {
        this.viewProvider = viewProvider;
        this.authenticationProvider = authenticationProvider;
        this.userDetailsService = userDetailsService;
        logoutBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        //viewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
    }

    @Override
    protected void init(VaadinRequest request) {

        Header header = new Header("FX :: 2");
        MHorizontalLayout top = new MHorizontalLayout(header, loginBtn)
                .withAlign(loginBtn, Alignment.MIDDLE_RIGHT)
                .withFullWidth();

        //In case of reloaded page
        if(getUI().getSession().getAttribute(USER_SESSION_ATTR) != null) {
            loginBtn.setVisible(false);
        }

        final Panel viewContainer = new Panel();
        viewContainer.setSizeFull();

        final CssLayout navigationBar = new CssLayout();
        navigationBar.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        //Build a root layout with a header, the view container and a nav bar at the bottom. The content of the view container
        //will be handled by the view navigator.
        final MVerticalLayout root = new MVerticalLayout(top, viewContainer, navigationBar).expand(viewContainer);
        root.setSizeFull();
        root.setMargin(true);
        root.setSpacing(true);
        setContent(root);

        Navigator navigator = new Navigator(this, viewContainer);
        navigator.addProvider(viewProvider);
        setNavigator(navigator);
        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                // Check if a user has logged in
                boolean isLoggedIn = getSession().getAttribute(USER_SESSION_ATTR) != null;
                boolean isLoginView = event.getNewView() instanceof FirstView;
                if (!isLoggedIn) {
                    navigationBar.removeComponent(logoutBtn);
                    navigationBar.removeComponent(listUsersBtn);
                    if (!isLoginView) {
                        getNavigator().navigateTo(FirstView.VIEW_NAME);
                        return false;
                    }
                } else { //Logged in
                    navigationBar.addComponent(logoutBtn);
                    navigationBar.addComponent(listUsersBtn);
                    if (isLoginView) {
                        //Logged in user should not be able to access login view
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
            }
        });

        getSession().setConverterFactory(new Fx2ConverterFactory());
    }

    private void login(LoginCredentials credentials, LoginForm loginForm) {
        try {
            Authentication ud = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
            LOG.info("User logged in: " + ud);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(credentials.getUsername());
            getSession().setAttribute(USER_SESSION_ATTR, userDetails);
            getUI().getNavigator().navigateTo(ListUserView.VIEW_NAME);
            loginForm.closePopup();
            Notification.show("Welcome " + ((User) userDetails).getFirstName(), WARNING_MESSAGE);
            loginBtn.setVisible(false);
        } catch (AuthenticationException ae) {
            LOG.debug("Authentication failed...");
            Notification.show("Sorry, no such username/password combo found", WARNING_MESSAGE);
        } finally {
            loginBtn.setEnabled(true);
        }
    }

    //Create a button which navigates to the specified view upon click.
    private Button createNavigationButton(String caption, final String viewName) {
        Button button = new MButton(caption);
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(event -> getUI().getNavigator().navigateTo(viewName));
        return button;
    }


}