package hoggaster.web.vaadin.views.user.depots;

import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import hoggaster.domain.InvalidResourceIdentifier;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.web.vaadin.AdminUI;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.Collection;

import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static reactor.bus.selector.Selectors.matchAll;

/**
 * Used to display a list of all depots for a user. TODO listen to events and update table.
 *
 * Created by svante.kumlien on 01.03.16.
 */
@Component
@ViewScope
public class ListDepotsComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListDepotsComponent.class);

    private final DepotService depotService;

    public final EventBus depotEventBus;

    //The user currently selected (not the logged in one...)
    private FormUser user;

    private Button addBtn = new MButton(FontAwesome.PLUS, this::addDepot).withDescription("Add a new depot");

    private Button deleteBtn = new ConfirmButton(FontAwesome.TRASH_O, "Are you sure you want to delete this depot?", this::deleteDepot).withStyleName("danger").withDescription("Delete the connection to this depot");

    private Button editBtn = new MButton(FontAwesome.PENCIL_SQUARE_O, this::editDepot).withDescription("Edit this depot");

    private final Registration registration;

    private final MTable<DbDepot> depotsTable = new MTable(DbDepot.class)
                .withProperties("name", "type", "balance", "currency", "marginRate", "marginAvailable", "numberOfOpenTrades", "realizedPl", "unrealizedPl",
            "lastSynchronizedWithBroker")
                .withColumnHeaders("Name", "Type", "Balance", "Currency", "Margin rate", "Margin available", "Number of open trades", "Realized profit/loss",
            "Unrealized profit/loss", "Last synchronized with broker")
                .withFullWidth();

    @Autowired
    public ListDepotsComponent(DepotService depotService,@Qualifier("depotEventBus") EventBus depotEventBus, AdminUI adminUI) {
        this.depotService = depotService;
        this.depotEventBus = depotEventBus;
        registration = depotEventBus.on(matchAll(), event -> {
            DbDepot dbDepot = (DbDepot) event.getData();
            LOG.info("Got an event on the depot bus: {}", event.getData());
            if(dbDepot.userId.equals(user.getId())) {
                LOG.info("And it's for us!: {}", event.getData());
                adminUI.access(() -> {
                    depotsTable.removeItem(dbDepot); //TODO fix this.
                    depotsTable.addItem(dbDepot);
                    depotsTable.markAsDirtyRecursive();
                    adminUI.push();
                });
            }
        });
    }

    @PreDestroy
    public void cleanUp() {
        LOG.info("Post construct called!!!");
        registration.cancel();
    }

    //Create the tab with the current open positions
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;
        final Collection<DbDepot> depots = depotService.findByUserId(user.getId());
        MVerticalLayout depotsTab = new MVerticalLayout().withSpacing(true);
        depotsTable.setBeans(depots);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        HorizontalLayout buttons = new MHorizontalLayout(addBtn, editBtn, deleteBtn);
        depotsTab.addComponents(buttons, depotsTable);
        depotsTab.expand(depotsTable);
        return depotsTab;
    }


    //Used to clean up the eventbus registrations we create
    public void deregisterAll() {
        cleanUp();
    }


    private void addDepot(Button.ClickEvent clickEvent) {
        DepotForm form = new DepotForm();
        form.openInModalPopup();
        form.setSavedHandler(depot -> {
            try {
                DbDepot dbDepot = depotService.createDepot(user.getId(), depot.getName(), depot.getBroker(), depot.getBrokerId(), depot.getType());
                depotService.syncDepotAsync(dbDepot);
                Notification.show("Success", "Your depot has been connected, wait a minute or two and it should show up in the depot listing", WARNING_MESSAGE);
            } catch (InvalidResourceIdentifier e) {
                Notification.show("Bad format of accountId", e.getMessage(), WARNING_MESSAGE);
            } catch (Exception e) {
                Notification.show("Something went wrong", e.getMessage(), WARNING_MESSAGE);
            }
            form.closePopup();
        });
        form.setResetHandler(fd -> {
            form.closePopup();
        });


    }

    private void deleteDepot(Button.ClickEvent clickEvent) {

    }

    private void editDepot(Button.ClickEvent clickEvent) {
    }


}
