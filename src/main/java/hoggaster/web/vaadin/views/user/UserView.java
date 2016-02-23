package hoggaster.web.vaadin.views.user;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.VerticalLayout;

/**
 * Main view for displaying info for a specific user.
 *
 * @author svante.kumlien
 */
@SpringView(name = UserView.VIEW_NAME)
public class UserView extends VerticalLayout implements View {
    public static final String VIEW_NAME = "UserView";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
