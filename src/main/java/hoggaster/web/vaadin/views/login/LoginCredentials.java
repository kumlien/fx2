package hoggaster.web.vaadin.views.login;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by svante2 on 2016-04-28.
 */
public class LoginCredentials {

    @NotBlank(message = "Your username goes here")
    private String username;

    @NotBlank(message = "Your password goes here")
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
