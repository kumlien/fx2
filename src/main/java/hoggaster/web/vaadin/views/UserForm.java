package hoggaster.web.vaadin.views;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import hoggaster.domain.users.Role;
import hoggaster.domain.users.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.vaadin.viritin.fields.CheckBoxGroup;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/**
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
        setSizeUndefined();
        setEntity(user);
    }

    @Override
    protected Component createContent() {
        roles.setCaptionGenerator(id -> {
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
        Collection<Role> roles;
        String password;
        String password2;

        public FormUser(User user) {
            this.id = user.getId();
            this.firstName = user.firstName;
            this.lastName = user.lastName;
            this.username = user.getUsername();
            this.email = user.email;
            this.roles = user.getAuthorities().stream().map(a -> Role.valueOf(a.getAuthority().toUpperCase())).collect(Collectors.toList());
            this.password = user.getPassword();
        }
        public FormUser(){}

        public Collection<Role> getRoles() {
            return roles;
        }

        public void setRoles(Collection<Role> roles) {
            this.roles = roles;
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

        public User getUser() {
            return new User(username, firstName, lastName, email, password, id, roles.stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toSet()));
        }
    }

    class SetToStringConverter implements Converter<String, Collection> {

        @Override
        public Collection convertToModel(String value, Class<? extends Collection> targetType, Locale locale) throws ConversionException {
            value = value != null ? value : "";
            return (Arrays.asList(value.split(",")).stream().map(String::trim).collect(Collectors.toSet()));
        }

        @Override
        public String convertToPresentation(Collection value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            return value != null ? value.stream().map(Object::toString).collect(Collectors.joining(",")).toString() : "";
        }

        @Override
        public Class<Collection> getModelType() {
            return Collection.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }
}
