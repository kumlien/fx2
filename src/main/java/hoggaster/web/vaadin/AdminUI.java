package hoggaster.web.vaadin;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.web.vaadin.views.LoginView;
import hoggaster.web.vaadin.views.user.ListUserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author svante2
 */

@Title("FX2 Admin interface")
@SpringUI(path = "fx2")
@Theme("fx2")
@Push(PushMode.MANUAL)
public class AdminUI extends UI {

    public static final Logger LOG = LoggerFactory.getLogger(AdminUI.class);

    //Get the views annotated as SpringView:s
    private final SpringViewProvider viewProvider;

    //The logout button, unset the user in the session and go back to login view
    private final Button logoutBtn = new MButton("Logout", e -> {
        getUI().getSession().setAttribute(LoginView.USER_SESSION_ATTR, null);
        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
    });

    //Button to navigate to the list with users
    private final Button listUsersBtn = createNavigationButton("List users", ListUserView.VIEW_NAME);

    @Autowired
    public AdminUI(SpringViewProvider viewProvider) {
        this.viewProvider = viewProvider;
        logoutBtn.addStyleName(ValoTheme.BUTTON_SMALL);
    }

    @Override
    protected void init(VaadinRequest request) {

        Header header = new Header("Welcome to FX2");

        final Panel viewContainer = new Panel();
        viewContainer.setSizeFull();

        final CssLayout navigationBar = new CssLayout();
        navigationBar.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        //Build a root layout with a header, the view container and a nav bar at the bottom. The content of the view container
        //will be handled by the view navigator.
        final MVerticalLayout root = new MVerticalLayout(header, viewContainer, navigationBar).expand(viewContainer);
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

                LOG.info("View changed to {}", event.getNewView());
                // Check if a user has logged in
                boolean isLoggedIn = getSession().getAttribute(LoginView.USER_SESSION_ATTR) != null;
                boolean isLoginView = event.getNewView() instanceof LoginView;
                if(!isLoggedIn) {
                    navigationBar.removeComponent(logoutBtn);
                    navigationBar.removeComponent(listUsersBtn);
                    if(!isLoginView) {
                        getNavigator().navigateTo(LoginView.VIEW_NAME);
                        return false;
                    }
                } else { //Logged in
                    navigationBar.addComponent(logoutBtn);
                    navigationBar.addComponent(listUsersBtn);
                    if(isLoginView) {
                        //Logged in user should not be able to access login view
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {}
        });
    }

    //Create a button which navigates to the specified view upon click.
    private Button createNavigationButton(String caption, final String viewName) {
        Button button = new MButton(caption);
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(event -> getUI().getNavigator().navigateTo(viewName));
        return button;
    }





}