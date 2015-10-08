package hoggaster.user;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by svante2 on 2015-10-03.
 */
public class CreateUserRequest {

    @NotBlank(message = "The username must not be blank")
    public final String username;

    @NotBlank(message = "The username must not be blank")
    public final String password1;

    @NotBlank(message = "The username must not be blank")
    public final String password2;

    @Email
    public final String email;

    @Email
    public final String email2;

    @NotBlank(message = "First name must not be blank")
    public final String firstName;

    @NotBlank(message = "Last name must not be blank")
    public final String lastName;

    public CreateUserRequest(String username, String password1, String password2, String email, String email2, String firstName, String lastName) {
        this.username = username;
        this.password1 = password1;
        this.password2 = password2;
        this.email = email;
        this.email2 = email2;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
