package hoggaster.web.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import hoggaster.domain.users.User;
import hoggaster.domain.users.UserService;
import hoggaster.web.security.MongoUserDetailsService;
import hoggaster.web.vaadin.views.UserForm.FormUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * Created by svante2 on 2016-02-18.
 */
@SpringView(name = ListUserView.VIEW_NAME)
public class ListUserView extends VerticalLayout implements View {
    public static final String VIEW_NAME = "users";

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
    private TextField pushed = new MTextField();


    @Autowired
    public ListUserView(UserService userService, MongoUserDetailsService userDetailsManager) {
        this.userService = userService;
        this.userDetailsManager = userDetailsManager;
    }


    @PostConstruct
    void init() {
        addComponent(
                new MVerticalLayout(
                        new MHorizontalLayout(addNew, edit, delete, pushed),
                        usersTable
                ).expand(usersTable)
        );
        listEntities();
        usersTable.addMValueChangeListener(e -> adjustActionButtonState());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        getUI().access(() -> {
                            pushed.setValue(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
                            getUI().push();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
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
            Notification.show("User updated", Notification.Type.ASSISTIVE_NOTIFICATION);
        } else {
            User user = new User(formUser.username, formUser.firstName, formUser.lastName, formUser.email, password, formUser.id, formUser.roles.stream().map(r -> new SimpleGrantedAuthority(r.toString())).collect(Collectors.toSet()));
            userDetailsManager.createUser(user);
            Notification.show("User created", Notification.Type.ASSISTIVE_NOTIFICATION);
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
