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
import hoggaster.web.vaadin.views.ListUserView;
import hoggaster.web.vaadin.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author svante2
 */

@Title("FX2 Admin interface")
@SpringUI(path = "admin")
@Theme("valo")
@Push(PushMode.MANUAL)
public class AdminUI extends UI {

    private final SpringViewProvider viewProvider;

    @Autowired
    public AdminUI(SpringViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @Override
    protected void init(VaadinRequest request) {


        Header header = new Header("Welcome to the FX2 Administration application");

        final Panel viewContainer = new Panel("Main...");
        viewContainer.setSizeFull();

        final CssLayout navigationBar = new CssLayout();
        navigationBar.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        navigationBar.addComponent(createNavigationButton("Users", ListUserView.VIEW_NAME));

        final MVerticalLayout root = new MVerticalLayout(header, viewContainer, navigationBar).expand(viewContainer);
        root.setSizeFull();
        root.setMargin(true);
        root.setSpacing(true);
        setContent(root);

        Navigator navigator = new Navigator(this, viewContainer);
        navigator.addProvider(viewProvider);
        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                // Check if a user has logged in
                boolean isLoggedIn = getSession().getAttribute("user") != null;
                boolean isLoginView = event.getNewView() instanceof LoginView;
                if(!isLoggedIn && !isLoginView) {
                    getNavigator().navigateTo(LoginView.VIEW_NAME);
                    return false;
                }
                if(isLoggedIn && isLoginView) {
                    //Logged in user should not be able to access loginview
                    return false;
                }
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {}
        });
    }

    private Button createNavigationButton(String caption, final String viewName) {
        Button button = new MButton(caption);
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(event -> getUI().getNavigator().navigateTo(viewName));
        return button;
    }





}