package hoggaster.web.vaadin.views.login;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * @author svante.kumlien
 */
@SpringComponent
@VaadinSessionScope
public class WebUser {

    private String username;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
