package hoggaster.domain.users.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Email(message = "Invalid email address")
    public final String email1;

    @Email(message = "Invalid email address")
    public final String email2;

    @NotBlank(message = "First name must not be blank")
    public final String firstName;

    @NotBlank(message = "Last name must not be blank")
    public final String lastName;

    @JsonCreator
    public CreateUserRequest(
            @JsonProperty("username") String username,
            @JsonProperty("password1")String password1,
            @JsonProperty("password2")String password2,
            @JsonProperty("email1")String email1,
            @JsonProperty("email2")String email2,
            @JsonProperty("firstName")String firstName,
            @JsonProperty("lastName")String lastName) {
        this.username = username;
        this.password1 = password1;
        this.password2 = password2;
        this.email1 = email1;
        this.email2 = email2;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
