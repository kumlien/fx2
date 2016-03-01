package hoggaster.web.vaadin.views.user;

import com.vaadin.event.Action;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import hoggaster.domain.users.User;
import hoggaster.domain.users.UserService;
import hoggaster.web.security.MongoUserDetailsService;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ASSISTIVE_NOTIFICATION;

/**
 * Displays a list of all users. Should probably only be available for admins...
 * Also gives the possibility to add/delete/edit users.
 *
 * Created by svante2 on 2016-02-18.
 */
@SpringView(name = ListUserView.VIEW_NAME)
public class ListUserView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "users";

    private static final Action VIEW_USER_DETAILS_ACTION = new Action("View details");

    private final UserService userService;

    private final MongoUserDetailsService userDetailsManager;

    MTable<FormUser> usersTable = new MTable<>(FormUser.class)
            .withProperties("firstName", "lastName", "email", "username")
            .withColumnHeaders("First name", "Last name", "Email", "Username")
            .setSortableProperties("firstName", "lastName")
            .withFullWidth();

    private Button addNew = new MButton(FontAwesome.PLUS, this::add);
    private Button edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
    private Button delete = new ConfirmButton(FontAwesome.TRASH_O, "Are you sure you want to delete the user?", this::remove);


    @Autowired
    public ListUserView(UserService userService, MongoUserDetailsService userDetailsManager) {
        this.userService = userService;
        this.userDetailsManager = userDetailsManager;
    }


    @PostConstruct
    void init() {
        addComponent(
                new MVerticalLayout(
                        new MHorizontalLayout(addNew, edit, delete),
                        usersTable
                ).expand(usersTable)
        );
        listEntities();
        usersTable.addMValueChangeListener(e -> adjustActionButtonState());
        usersTable.addActionHandler(new Action.Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                if(target != null) {
                    return new Action[]{VIEW_USER_DETAILS_ACTION};
                }
                return new Action[]{};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                getUI().getSession().setAttribute(UserView.SESSION_ATTRIBUTE_SELECTED_USER, target);
                getUI().getNavigator().navigateTo(UserView.VIEW_NAME);
            }
        });
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

    private void listEntities() {
        usersTable.setBeans(userService.findAll().stream().map(FormUser::new).collect(Collectors.toList()));
        adjustActionButtonState();
    }


    protected void adjustActionButtonState() {
        boolean hasSelection = usersTable.getValue() != null;
        edit.setEnabled(hasSelection);
        delete.setEnabled(hasSelection);
    }

    public void add(Button.ClickEvent clickEvent) {
        edit(new FormUser());
    }

    public void edit(Button.ClickEvent e) {
        edit(usersTable.getValue());
    }

    public void remove(Button.ClickEvent e) {
        userDetailsManager.deleteUser(usersTable.getValue().username);
        usersTable.setValue(null);
        listEntities();
    }

    //Display a form and set our methods as handlers
    protected void edit(final FormUser user) {
        UserForm userForm = new UserForm(user);
        userForm.openInModalPopup();
        userForm.setSavedHandler(this::saveUser);
        userForm.setResetHandler(this::resetEntry);
    }

    public void saveUser(FormUser formUser) {
        String password;
        if (!StringUtils.hasText(formUser.password)) { //No pwd entered, use the stored one from db. todo store it in the formuser
            password = userDetailsManager.loadUserByUsername(formUser.username).getPassword();
        } else {
            password = userDetailsManager.encode(formUser.password);
        }
        if (StringUtils.hasText(formUser.id)) { //Existing user
            User user = new User(formUser.username, formUser.firstName, formUser.lastName, formUser.email, password, formUser.id, formUser.roles.stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toSet()));
            userDetailsManager.updateUser(user);
            Notification.show("User updated", ASSISTIVE_NOTIFICATION);
        } else {
            User user = new User(formUser.username, formUser.firstName, formUser.lastName, formUser.email, password, formUser.id, formUser.roles.stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toSet()));
            userDetailsManager.createUser(user);
            Notification.show("User created", ASSISTIVE_NOTIFICATION);
        }
        listEntities();
        closeWindow();
    }

    public void resetEntry(FormUser user) {
        listEntities();
        closeWindow();
    }

    protected void closeWindow() {
        getUI().getWindows().stream().forEach(w -> getUI().removeWindow(w));
    }
}
