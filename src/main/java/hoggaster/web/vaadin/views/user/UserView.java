package hoggaster.web.vaadin.views.user;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import hoggaster.web.vaadin.views.UserForm.FormUser;

/**
 * Main view for displaying info for a specific user.
 *
 * @author svante.kumlien
 */
@SpringView(name = UserView.VIEW_NAME)
public class UserView extends VerticalLayout implements View {
    public static final String VIEW_NAME = "UserView";
    public static final String SELECTED_USER = "SelectedUser";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        FormUser user = (FormUser) getUI().getSession().getAttribute(SELECTED_USER);
        Notification.show("Got a user: " + user.getUsername());
    }
}
