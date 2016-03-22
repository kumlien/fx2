package hoggaster.web.vaadin.views.user.depots;

import com.vaadin.spring.annotation.ViewScope;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to display a list of all depots for a user.
 *
 * Created by svante.kumlien on 01.03.16.
 */
@Component
@ViewScope
public class ListDepotsComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListDepotsComponent.class);

    private final DepotService depotService;

    private final Map<CurrencyPair, Registration> registrations = new ConcurrentHashMap<>();

    public final EventBus priceEventBus;

    private MTable<DbDepot> depotsTable;

    private FormUser user;

    @Autowired
    public ListDepotsComponent(DepotService depotService, @Qualifier("priceEventBus") EventBus priceEventBus) {
        this.depotService = depotService;
        this.priceEventBus = priceEventBus;
    }

    //Create the tab with the current open positions
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        final Collection<DbDepot> depots = depotService.findByUserId(user.getId());
        MVerticalLayout depotsTab = new MVerticalLayout();
        MTable<DbDepot> depotsTable = new MTable(DbDepot.class)
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

    private void listEntities() {
        Collection<DbDepot> allDepots =  depotService.findByUserId(user.getId());
        depotsTable.setBeans(allDepots);
    }

    //Used to clean up the eventbus registrations we create
    public void deregisterAll() {
        final Collection<Registration> r = registrations.values();
        if (r != null) {
            LOG.debug("About to deregisterAll {} registrations", this.registrations.size());
            r.forEach(Registration::cancel);
        } else {
            LOG.debug("No registrations found");
        }
        registrations.clear();
    }

    private final void deregister(CurrencyPair currencyPair) {
        final Registration registration = registrations.get(currencyPair);
        if(registration != null) {
            registration.cancel();
            registrations.remove(currencyPair);
        }
    }
}
