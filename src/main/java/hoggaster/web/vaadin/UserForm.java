package hoggaster.web.vaadin;

import com.google.common.collect.Sets;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import hoggaster.domain.users.User;
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
public class UserForm extends AbstractForm<User> {

    TextField firstName = new MTextField("First name");
    TextField email = new MTextField("Email");
    TextField lastName = new MTextField("Last name");
    TextField username = new MTextField("Username");
    TextField authorities = new MTextField("Authorities");
    PasswordField password = new MPasswordField("Password");
    PasswordField password2 = new MPasswordField("Password (repeated)");

    public UserForm(User user) {
        setSizeUndefined();
        setEntity(user);
    }

    @Override
    protected Component createContent() {
        authorities.setConverter(new SetToStringConverter());
        return new MVerticalLayout(
                new MFormLayout(
                        firstName,
                        lastName,
                        username,
                        email,
                        authorities,
                        password,
                        password2
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
    }

    class SetToStringConverter implements Converter<String, Collection> {

        @Override
        public Collection convertToModel(String value, Class<? extends Collection> targetType, Locale locale) throws ConversionException {
            value = value != null ? value : "";
            return (Arrays.asList(value.split(",")).stream().map(String::trim).collect(Collectors.toSet()));
        }

        @Override
        public String convertToPresentation(Collection value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            return value.stream().collect(Collectors.joining(",")).toString();
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
