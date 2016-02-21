package hoggaster.web.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import hoggaster.domain.users.UserService;
import hoggaster.web.vaadin.views.UserForm.FormUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.provisioning.UserDetailsManager;
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

    private final UserDetailsManager userDetailsManager;

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
    public ListUserView(UserService userService, UserDetailsManager userDetailsManager) {
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
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
        //userService.delete(usersTable.getValue());
        usersTable.setValue(null);
        listEntities();
    }

    protected void edit(final FormUser user) {
        UserForm userForm = new UserForm(user);
        userForm.openInModalPopup();
        userForm.setSavedHandler(this::saveEntry);
        userForm.setResetHandler(this::resetEntry);
    }

    public void saveEntry(FormUser user) {
        if(StringUtils.hasText(user.id)) {
            userDetailsManager.updateUser(user.getUser());
        } else {
            userDetailsManager.createUser(user.getUser());
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
