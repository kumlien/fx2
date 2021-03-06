package hoggaster.web.vaadin.views.user;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.depots.ListDepotsComponent;
import hoggaster.web.vaadin.views.user.positions.ListPositionsComponent;
import hoggaster.web.vaadin.views.user.robots.ListRobotsComponent;
import hoggaster.web.vaadin.views.user.trades.ListTradesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.annotation.PreDestroy;

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
    public final BrokerConnection brokerConnection;

    private final ListPositionsComponent listPositionsComponent; //gui component used to display the list of positions
    private final ListDepotsComponent listDepotsComponent;
    private final ListTradesComponent listTradesComponent;
    private final ListRobotsComponent listRobotsComponent;


    @Autowired
    public UserView(@Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, ListPositionsComponent listPositionsComponent, ListDepotsComponent listDepotsComponent, ListTradesComponent listTradesComponent, ListRobotsComponent listRobotsComponent) {
        this.brokerConnection = brokerConnection;
        this.listDepotsComponent = listDepotsComponent;
        this.listPositionsComponent = listPositionsComponent;
        this.listTradesComponent = listTradesComponent;
        this.listRobotsComponent = listRobotsComponent;
    }

    @PreDestroy
    public void cleanUp() {
        LOG.info("Cleanup called!!");
        listDepotsComponent.deregisterAll();
        listPositionsComponent.deregisterAll();
        listTradesComponent.deregisterAll();
        listRobotsComponent.deregisterAll();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        LOG.info("Enter: " + event.getViewName());

        FormUser user = (FormUser) getUI().getSession().getAttribute(SESSION_ATTRIBUTE_SELECTED_USER);

        Header header = new Header("Details for " + user.getFirstName() + " " + user.getLastName());
        header.setHeaderLevel(3);

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(TABSHEET_PADDED_TABBAR);

        tabSheet.addTab(listDepotsComponent.setUp(user, this), "Depots");
        tabSheet.addTab(listPositionsComponent.setUp(user, this), "Positions");
        tabSheet.addTab(listTradesComponent.setUp(user, this),"Trades");
        tabSheet.addTab(createTransactionsTab(), "Transactions");
        tabSheet.addTab(listRobotsComponent.setUp(user,this), "Robots");


        addComponents(header, tabSheet);
        expand(tabSheet);

        getUI().addDetachListener(e -> {
            LOG.info("Deregister since the ui is detached");
            cleanUp();
        });
    }

    private Component createTransactionsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

}
