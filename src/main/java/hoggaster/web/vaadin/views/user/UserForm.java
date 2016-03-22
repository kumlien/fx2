package hoggaster.web.vaadin.views.user;

import com.google.common.collect.Sets;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import hoggaster.domain.users.Role;
import hoggaster.domain.users.User;
import org.springframework.util.StringUtils;
import org.vaadin.viritin.fields.CheckBoxGroup;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Component used to add/edit a user
 *
 * Created by svante2 on 2016-02-14.
 */
public class UserForm extends AbstractForm<UserForm.FormUser> {

    TextField firstName = new MTextField("First name");
    TextField email = new MTextField("Email");
    TextField lastName = new MTextField("Last name");
    TextField username = new MTextField("Username");
    CheckBoxGroup<Role> roles = new CheckBoxGroup<>("Roles");

    PasswordField password = new MPasswordField("Password");
    PasswordField password2 = new MPasswordField("Password again");

    public UserForm(FormUser user) {
        firstName.setRequired(true);
        lastName.setRequired(true);
        username.setRequired(true); //TODO add validator for unique username?
        email.addValidator(new EmailValidator("Please provide a valid email address"));
        email.setRequired(true);

        addValidator(fu -> {
            if(fu.id == null && !StringUtils.hasText(password.getValue())) {
                throw new InvalidValueException("You must specify a password for a new user");
            }
            if(password.getValue() != null && !password.getValue().equals(password2.getValue())) {
                throw new InvalidValueException("The passwords doesn't match");
            }
        }, password, password2);

        setSizeUndefined();
        setEntity(user);
    }

    @Override
    protected Component createContent() {
        roles.setCaptionGenerator(id -> { //Fix the roles options group
            if(id == Role.ADMIN) return "Admin";
            if(id == Role.USER) return  "User";
            return "Unknown: " + id;
        });
        roles.setOptions(Role.values());
        roles.setSizeFull();


        return new MVerticalLayout(
                new MFormLayout(
                        firstName,
                        lastName,
                        username,
                        email,
                        roles,
                        password,
                        password2
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
    }


    public static class FormUser {
        String id;
        String firstName;
        String lastName;
        String username;
        String email;
        Collection<Role> roles = Sets.newHashSet();
        String password;
        String password2;

        public FormUser(User user) {
            this.id = user.getId();
            this.firstName = user.firstName;
            this.lastName = user.lastName;
            this.username = user.getUsername();
            this.email = user.email;
            this.roles = user.getAuthorities().stream().map(a -> Role.valueOf(a.getAuthority().toUpperCase())).collect(Collectors.toList());
        }
        public FormUser(){}

        public String getId() {
            return id;
        }

        public Collection<Role> getRoles() {
            return roles;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword2() {
            return password2;
        }

        public void setPassword2(String password2) {
            this.password2 = password2;
        }
    }
}
