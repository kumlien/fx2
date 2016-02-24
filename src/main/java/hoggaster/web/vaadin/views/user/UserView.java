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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
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
 *     <li>Depots</li>
 *     <li>Positions</li>
 *     <li>Trades</li>
 *     <li>Robots</li>
 * </ul>
 *
 * @author svante.kumlien
 */
@SpringView(name = UserView.VIEW_NAME)
public class UserView extends MVerticalLayout implements View {
    public static final String VIEW_NAME = "UserView";
    public static final String SESSION_ATTRIBUTE_SELECTED_USER = "SelectedUser";

    private final DepotService depotService;

    private final PriceService priceService;

    private final EventBus priceEventBus;

    @Autowired
    public UserView(DepotService depotService, PriceService priceService, @Qualifier("priceEventBus") EventBus priceEventBus) {
        this.depotService = depotService;
        this.priceService = priceService;
        this.priceEventBus = priceEventBus;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        System.out.println("enter: " + event.getViewName());
        FormUser user = (FormUser) getUI().getSession().getAttribute(SESSION_ATTRIBUTE_SELECTED_USER);


        Header header = new Header("Details for " + user.getFirstName() + " " + user.getLastName());
        header.setHeaderLevel(2);

        Collection<DbDepot> depots = depotService.findByUserId(user.getId());

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(TABSHEET_FRAMED);
        tabSheet.addStyleName(TABSHEET_PADDED_TABBAR);
        tabSheet.addTab(createDepotsTab(depots), "Depots");
        tabSheet.addTab(createPositionsTab(depots), "Positions");
        tabSheet.addTab(createTradesTab(), "Trades");
        tabSheet.addTab(createRobotsTab(), "Robots");

        addComponents(header, tabSheet);
        expand(tabSheet);
    }

    private Component createRobotsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createTradesTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createPositionsTab(Collection<DbDepot> depots) {
        MVerticalLayout tab = new MVerticalLayout();
        MTable<Position> table = new MTable<>(Position.class)
                .withCaption("Your open positions:")
                .withProperties("currencyPair", "side", "quantity", "averagePricePerShare")
                .withColumnHeaders("Currency pair", "Side", "Quantity", "Average price")
                .withGeneratedColumn("Current price", new MTable.SimpleColumnGenerator<Position>() {
                    @Override
                    public Object generate(Position entity) {
                        Label label = new Label(priceService.getLatestPriceForCurrencyPair(entity.currencyPair).ask.toString());
                        System.out.println("new registration...");
                        priceEventBus.on($("prices."+entity.currencyPair), e -> { //TODO how do we de-register...?
                            getUI().access(() -> {
                                if(!label.getValue().equals(((Price)e.getData()).ask.toString())) {
                                    label.setValue(((Price) e.getData()).ask.toString());
                                    getUI().push();
                                }
                            });
                        });
                        return label;
                    }
                })
                .withGeneratedColumn("Time", new MTable.SimpleColumnGenerator<Position>() {
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
                .withProperties("name", "type", "balance", "currency", "marginRate", "marginAvailable","numberOfOpenTrades","realizedPl","unrealizedPl", "lastSynchronizedWithBroker")
                .withColumnHeaders("Name", "Type", "Balance", "Currency", "Margin rate", "Margin available", "Number of open trades", "Realized profit/loss", "Unrealized profit/loss", "Last synchronized with broker")
                .withFullWidth();

        depotsTable.setBeans(depots);
        depotsTab.addComponents(depotsTable);
        depotsTab.expand(depotsTable);
        return depotsTab;
    }
}
