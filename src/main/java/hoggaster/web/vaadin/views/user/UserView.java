package hoggaster.web.vaadin.views.user;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DepotService;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.depots.ListDepotsComponent;
import hoggaster.web.vaadin.views.user.positions.ListPositionsComponent;
import hoggaster.web.vaadin.views.user.trades.ListTradesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

import static com.vaadin.ui.themes.ValoTheme.TABSHEET_PADDED_TABBAR;

/**
 * Main view for displaying info for a specific user.
 *
 * <p>
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

    private static final Logger LOG = LoggerFactory.getLogger(UserView.class);
    private final DepotService depotService;
    public final BrokerConnection brokerConnection;

    private final ListPositionsComponent listPositionsComponent; //gui component used to display the list of positions
    private final ListDepotsComponent listDepotsComponent;
    private final ListTradesComponent listTradesComponent;


    @Autowired
    public UserView(DepotService depotService, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, ListPositionsComponent listPositionsComponent, ListDepotsComponent listDepotsComponent, ListTradesComponent listTradesComponent) {
        this.depotService = depotService;
        this.brokerConnection = brokerConnection;
        this.listDepotsComponent = listDepotsComponent;
        this.listPositionsComponent = listPositionsComponent;
        this.listTradesComponent = listTradesComponent;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        LOG.info("Enter: " + event.getViewName());

        FormUser user = (FormUser) getUI().getSession().getAttribute(SESSION_ATTRIBUTE_SELECTED_USER);

        Header header = new Header("Details for " + user.getFirstName() + " " + user.getLastName());
        header.setHeaderLevel(3);

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(TABSHEET_PADDED_TABBAR);

        final Component positionsTab = listPositionsComponent.setUp(user, this);
        final Component tradesTab = listTradesComponent.setUp(user, this);
        tabSheet.addTab(listDepotsComponent.setUp(user, this), "Depots");
        tabSheet.addTab(positionsTab, "Positions");
        //tabSheet.addTab(
        tabSheet.addTab(createTransactionsTab(), "Transactions");
        tabSheet.addTab(createRobotsTab(), "Robots");

        tabSheet.addSelectedTabChangeListener(e -> {
            final Component selectedTab = e.getTabSheet().getSelectedTab();
            if (selectedTab != positionsTab) {
                LOG.info("Positions tab is selected...");
                listPositionsComponent.deregisterAll();;
            } else if(selectedTab != listTradesComponent) {
                LOG.info("{} is selected tab", selectedTab);
                listPositionsComponent.deregisterAll();
            }
        });

        addComponents(header, tabSheet);
        expand(tabSheet);

        getUI().addDetachListener(e -> {
            LOG.info("Deregister since the ui is detached");
            listPositionsComponent.deregisterAll();
            listTradesComponent.deregisterAll();
        });
    }



    private Component createRobotsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createTransactionsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

}
