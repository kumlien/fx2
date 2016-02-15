package hoggaster.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import hoggaster.domain.users.User;
import hoggaster.domain.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author svante2
 */

@Title("FX2 Admin interface")
@SpringUI(path = "admin")
@Theme("valo")
public class AdminUI extends UI {

    private final UserService userService;

    MTable<User> usersTable = new MTable<>(User.class)
            .withProperties("firstName", "lastName", "email", "username")
            .withColumnHeaders("First name", "Last name", "Email", "Username")
            .setSortableProperties("firstName", "lastName")
            .withFullWidth();

    private Button addNew = new MButton(FontAwesome.PLUS, this::add);
    private Button edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
    private Button delete = new ConfirmButton(FontAwesome.TRASH_O,
            "Are you sure you want to delete the user?", this::remove);

    @Autowired
    public AdminUI(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void init(VaadinRequest request) {
        setContent(
                new MVerticalLayout(
                        new RichText().setRichText("hej hopp"),
                        new MHorizontalLayout(addNew, edit, delete),
                        usersTable
                ).expand(usersTable)
        );
        listEntities();
        usersTable.addMValueChangeListener(e -> adjustActionButtonState());
    }

    private void listEntities() {
        usersTable.setBeans(userService.findAll());
        adjustActionButtonState();
    }


    protected void adjustActionButtonState() {
        boolean hasSelection = usersTable.getValue() != null;
        edit.setEnabled(hasSelection);
        delete.setEnabled(hasSelection);
    }


    public void add(Button.ClickEvent clickEvent) {
        edit(new User());
    }

    public void edit(Button.ClickEvent e) {
        edit(usersTable.getValue());
    }

    public void remove(Button.ClickEvent e) {
        userService.delete(usersTable.getValue());
        usersTable.setValue(null);
        listEntities();
    }

    protected void edit(final User user) {
        UserForm userForm = new UserForm(user);
        userForm.openInModalPopup();
        userForm.setSavedHandler(this::saveEntry);
        userForm.setResetHandler(this::resetEntry);
    }

    public void saveEntry(User user) {
        userService.update(user);
        listEntities();
        closeWindow();
    }

    public void resetEntry(User user) {
        listEntities();
        closeWindow();
    }

    protected void closeWindow() {
        getWindows().stream().forEach(w -> removeWindow(w));
    }
}