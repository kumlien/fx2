package hoggaster.web.vaadin.views.user;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.depots.Position;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.web.vaadin.views.UserForm.FormUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTable.SimpleColumnGenerator;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.vaadin.ui.themes.ValoTheme.TABSHEET_FRAMED;
import static com.vaadin.ui.themes.ValoTheme.TABSHEET_PADDED_TABBAR;
import static reactor.bus.selector.Selectors.$;

/**
 * Main view for displaying info for a specific user.
 *
 * Contains a top header with user info
 * Below is a tabbed pane with tabs for
 * <ul>
 * <li>Depots</li>
 * <li>Positions</li>
 * <li>Trades</li>
 * <li>Robots</li>
 * </ul>
 *
 * @author svante.kumlien
 */
@SpringView(name = UserView.VIEW_NAME)
public class UserView extends MVerticalLayout implements View {

    public static final String VIEW_NAME = "UserView";
    public static final String SESSION_ATTRIBUTE_SELECTED_USER = "SelectedUser";

    public static final Logger LOG = LoggerFactory.getLogger(UserView.class);

    private final DepotService depotService;

    private final PriceService priceService;

    private final EventBus priceEventBus;

    private final Map<UserView, List<Registration>> registrations = new ConcurrentHashMap<>();

    @Autowired
    public UserView(DepotService depotService, PriceService priceService, @Qualifier("priceEventBus") EventBus priceEventBus) {
        this.depotService = depotService;
        this.priceService = priceService;
        this.priceEventBus = priceEventBus;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        LOG.info("Enter: " + event.getViewName());
        FormUser user = (FormUser) getUI().getSession().getAttribute(SESSION_ATTRIBUTE_SELECTED_USER);


        Header header = new Header("Details for " + user.getFirstName() + " " + user.getLastName());
        header.setHeaderLevel(2);

        Collection<DbDepot> depots = depotService.findByUserId(user.getId());

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(TABSHEET_FRAMED);
        tabSheet.addStyleName(TABSHEET_PADDED_TABBAR);

        final Component positionsTab = createPositionsTab(depots);
        tabSheet.addTab(createDepotsTab(depots), "Depots");
        tabSheet.addTab(positionsTab, "Positions");
        tabSheet.addTab(createTradesTab(), "Trades");
        tabSheet.addTab(createRobotsTab(), "Robots");

        tabSheet.addSelectedTabChangeListener(e -> {
            LOG.info("Selected tab change detected, selected tab is now {}", e.getTabSheet().getSelectedTab());
            if (e.getTabSheet().getSelectedTab() == positionsTab) {
                LOG.info("Positions tab is selected...");
            } else {
                LOG.info("Deregister since selected tab is not positions tab");
                deregister();
            }
        });

        addComponents(header, tabSheet);
        expand(tabSheet);

        getUI().addDetachListener(e -> {
            LOG.info("Deregister since the ui is detached");
            deregister();
        });

    }

    //Used to clean up the eventbus registrations we create
    private void deregister() {
        final List<Registration> r = this.registrations.get(this);
        if(r != null) {
            LOG.info("About to deregister {} registrations", this.registrations.size());
            r.forEach(Registration::cancel);
            registrations.remove(this);
        } else {
            LOG.info("No registrations found");
        }
    }

    private Component createRobotsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createTradesTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    //Create the tab with the current open positions
    private Component createPositionsTab(Collection<DbDepot> depots) {
        MVerticalLayout tab = new MVerticalLayout();
        LOG.info("Tab created: {}", tab.hashCode());
        MTable<Position> table = new MTable<>(Position.class)
                .withCaption("Your open positions:")
                .withProperties("currencyPair", "side", "quantity", "averagePricePerShare")
                .withColumnHeaders("Currency pair", "Side", "Quantity", "Average price")
                .withGeneratedColumn("Current price", new SimpleColumnGenerator<Position>() {
                    @Override
                    public Object generate(Position entity) {
                        Label label = new Label(priceService.getLatestPriceForCurrencyPair(entity.currencyPair).ask.toString());
                        LOG.info("Creating new registration for entity {}", entity.currencyPair);
                        final Registration registration = priceEventBus.on($("prices." + entity.currencyPair), e -> { //TODO how do we de-register...?
                            if (getUI() == null) { //Continue to push until gui is gone
                                LOG.info("Deregister since ui is null in eventbus registration");
                                deregister();
                                return;
                            }
                            getUI().access(() -> {
                                if (!label.getValue().equals(((Price) e.getData()).ask.toString())) {
                                    LOG.info("Pushing new price to gui");
                                    label.setValue(((Price) e.getData()).ask.toString());
                                    getUI().push();
                                }
                            });
                        });
                        List r = registrations.get(this) != null ? registrations.get(this) : new ArrayList();
                        LOG.info("Deregister old registrations before adding new one");
                        deregister();
                        r.add(registration);
                        registrations.put(UserView.this, r);
                        return label;
                    }
                })
                .withGeneratedColumn("Time", new SimpleColumnGenerator<Position>() {
                    @Override
                    public Object generate(Position entity) {
                        return priceService.getLatestPriceForCurrencyPair(entity.currencyPair).time.truncatedTo(ChronoUnit.SECONDS);
                    }
                })
                .withFullWidth();
        table.setBeans(depots.stream().map(DbDepot::getPositions).flatMap(Collection::stream).collect(Collectors.toList()));
        tab.addComponent(table);
        tab.expand(table);
        return tab;
    }

    private MVerticalLayout createDepotsTab(Collection<DbDepot> depots) {
        MVerticalLayout depotsTab = new MVerticalLayout();
        MTable<DbDepot> depotsTable = new MTable(DbDepot.class)
                .withCaption("Depots")
                .withProperties("name", "type", "balance", "currency", "marginRate", "marginAvailable", "numberOfOpenTrades", "realizedPl", "unrealizedPl",
                        "lastSynchronizedWithBroker")
                .withColumnHeaders("Name", "Type", "Balance", "Currency", "Margin rate", "Margin available", "Number of open trades", "Realized profit/loss",
                        "Unrealized profit/loss", "Last synchronized with broker")
                .withFullWidth();

        depotsTable.setBeans(depots);
        depotsTab.addComponents(depotsTable);
        depotsTab.expand(depotsTable);
        return depotsTab;
    }
}
