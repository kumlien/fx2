package hoggaster.web.vaadin.views.user;

import com.vaadin.event.Action;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.positions.ClosePositionResponse;
import hoggaster.domain.positions.Position;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.oanda.exceptions.TradingHaltedException;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTable.SimpleColumnGenerator;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ASSISTIVE_NOTIFICATION;
import static com.vaadin.ui.themes.ValoTheme.TABSHEET_FRAMED;
import static com.vaadin.ui.themes.ValoTheme.TABSHEET_PADDED_TABBAR;
import static reactor.bus.selector.Selectors.$;

/**
 * Main view for displaying info for a specific user.
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

    public static final Logger LOG = LoggerFactory.getLogger(UserView.class);
    private static final Action CLOSE_POSITION_ACTION = new Action("Close this position");
    private final DepotService depotService;
    private final PriceService priceService;
    private final EventBus priceEventBus;
    private final BrokerConnection brokerConnection;
    private final Map<UserView, List<Registration>> registrations = new ConcurrentHashMap<>();

    @Autowired
    public UserView(DepotService depotService, PriceService priceService, @Qualifier("priceEventBus") EventBus priceEventBus,
            @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.depotService = depotService;
        this.priceService = priceService;
        this.priceEventBus = priceEventBus;
        this.brokerConnection = brokerConnection;
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
        if (r != null) {
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
        final String deafaultPriceLabel = "Waiting for price updates...";
        MVerticalLayout tab = new MVerticalLayout();
        LOG.info("Tab created: {}", tab.hashCode());
        MTable<UIPosition> table = new MTable<>(UIPosition.class)
                .withCaption("Your open positions:")
                .withProperties("depotName", "currencyPair", "side", "quantity", "averagePricePerShare")
                .withColumnHeaders("Depot", "Currency pair", "Side", "Quantity", "Average price")
                .withGeneratedColumn("Current price", new SimpleColumnGenerator<UIPosition>() {
                    @Override
                    public Object generate(UIPosition entity) {
                        Label label = new Label("Waiting for price updates...");
                        label.addStyleName("pushbox");
                        //Label label = new Label(priceService.getLatestPriceForCurrencyPair(CurrencyPair.valueOf(entity.currencyPair)).ask.toString());
                        LOG.info("Creating new registration for entity {}", entity.getCurrencyPair());
                        final Registration registration = priceEventBus.on($("prices." + entity.getCurrencyPair()), e -> {
                            if (getUI() == null) { //Continue to push until gui is gone
                                LOG.info("Deregister since ui is null in eventbus registration");
                                deregister();
                                return;
                            }

                            Double previous = Double.valueOf(label.getValue().equals(deafaultPriceLabel) ? "0" : label.getValue());
                            Double current = ((Price) e.getData()).ask.doubleValue();
                            LOG.info("Got a new price: {}", e.getData());
                            getUI().access(() -> {
                                label.setValue(current.toString());
                                label.removeStyleName("pushPositive");
                                label.removeStyleName("pushNegative");
                                if (current > previous) {
                                    label.addStyleName("pushPositive");
                                } else if (current < previous) {
                                    label.addStyleName("pushNegative");
                                }
                                LOG.info(label.getStyleName());
                                getUI().push(); //If price same for second time in a row we dont need to push
                            });
                            try {
                                Thread.sleep(1000); //TODO Gahhhh...
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            getUI().access(() -> {
                                label.removeStyleName("pushPositive");
                                label.removeStyleName("pushNegative");
                                getUI().push();
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
                .withGeneratedColumn("Time", new SimpleColumnGenerator<UIPosition>() {
                    @Override
                    public Object generate(UIPosition entity) {
                        Optional<Price> lastPrice = priceService.getLatestPriceForCurrencyPair(entity.getCurrencyPair());
                        if (lastPrice.isPresent()) {
                            return lastPrice.get().time.truncatedTo(ChronoUnit.SECONDS);
                        }
                        return "n/a";
                    }
                })
                .withFullWidth();
        table.setColumnExpandRatio("depotName", 0.1f);
        List<UIPosition> allPositions = new ArrayList<>();
        for (DbDepot depot : depots) {
            allPositions.addAll(depot.getPositions().stream().map(p -> new UIPosition(depot, p)).collect(Collectors.toList()));
        }
        table.setBeans(allPositions);
        table.setSelectable(true);
        table.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                if (target != null) {
                    return new Action[] { CLOSE_POSITION_ACTION };
                }
                return new Action[] {};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                ConfirmDialog.show(getUI(), "Really close position?",
                        "Are you really sure you want to close your " + ((UIPosition) target).getCurrencyPair() + " position?",
                        "Yes", "No", dialog -> {
                    if (dialog.isConfirmed()) {
                        try {
                            ClosePositionResponse response = brokerConnection.closePosition(Integer.valueOf(((UIPosition) target).getBrokerDepotId()), ((UIPosition) target).getCurrencyPair());
                            LOG.info("Position closed {}, {}", sender, target);
                            Notification.show("Your position in " + response.currencyPair + " was closed to a price of " + response.price, ASSISTIVE_NOTIFICATION);
                        } catch (TradingHaltedException e) {
                            Notification.show("Sorry, unable to close the position since the trading is currently halted", Notification.Type.ERROR_MESSAGE);
                        }
                    }
                });
            }
        });
        tab.addComponents(table);
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

    public class UIPosition {

        final DbDepot depot;
        final Position position;

        UIPosition(DbDepot depot, Position position) {
            this.depot = depot;
            this.position = position;
        }

        public String getBrokerDepotId() {
            return depot.brokerId;
        }

        public String getDepotName() {
            return depot.getName();
        }

        public CurrencyPair getCurrencyPair() {
            return position.currencyPair;
        }

        public OrderSide getSide() {
            return position.side;
        }

        public BigDecimal getQuantity() {
            return position.getQuantity();
        }

        public BigDecimal getAveragePricePerShare() {
            return position.getAveragePricePerShare();
        }

    }
}
