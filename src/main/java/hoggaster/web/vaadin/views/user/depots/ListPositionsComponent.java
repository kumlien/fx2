package hoggaster.web.vaadin.views.user.depots;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.positions.ClosePositionResponse;
import hoggaster.domain.prices.Price;
import hoggaster.oanda.exceptions.TradingHaltedException;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTable.SimpleColumnGenerator;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.registry.Registration;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static reactor.bus.selector.Selectors.$;

/**
 * Used to display a list of all positions for a user.
 *
 * Created by svante.kumlien on 01.03.16.
 */
@Component
@ViewScope
public class ListPositionsComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListPositionsComponent.class);

    private static final Action CLOSE_POSITION_ACTION = new Action("Close this position");

    private final DepotService depotService;

    private final Map<CurrencyPair, Registration> registrations = new ConcurrentHashMap<>();

    private MTable<UIPosition> positionsTable;

    private FormUser user;

    @Autowired
    public ListPositionsComponent(DepotService depotService) {
        this.depotService = depotService;
    }

    //Create the tab with the current open positions
    public MVerticalLayout setUp(FormUser user, UserView userView) {
        this.user = user;
        final String defaultPriceLabel = "Fetching...";
        MVerticalLayout tab = new MVerticalLayout();
        positionsTable = new MTable<>(UIPosition.class)
                .withCaption("Your open positions:")
                .withProperties("depotName", "currencyPair", "side", "quantity", "averagePricePerShare")
                .withColumnHeaders("Depot", "Currency pair", "Side", "Quantity", "Average price")
                .withGeneratedColumn("Current price", new SimpleColumnGenerator<UIPosition>() {
                    @Override
                    public Object generate(UIPosition position) {
                        Label label = new Label(defaultPriceLabel);
                        label.addStyleName("pushbox");
                        LOG.info("Creating new registration for position {}", position.getCurrencyPair());
                        final Registration registration = userView.priceEventBus.on($("prices." + position.getCurrencyPair()), e -> {
                            if (userView.getUI() == null) { //Continue to push until gui is gone
                                LOG.info("Deregister since ui is null in eventbus registration");
                                deregisterAll();
                                return;
                            }

                            Double previous = Double.valueOf(label.getValue().equals(defaultPriceLabel) ? "0" : label.getValue());
                            Double current = ((Price) e.getData()).ask.doubleValue();
                            LOG.info("Got a new price: {}", e.getData());
                            userView.getUI().access(() -> {
                                label.setValue(current.toString());
                                label.removeStyleName("pushPositive");
                                label.removeStyleName("pushNegative");
                                if (current > previous) {
                                    label.addStyleName("pushPositive");
                                } else if (current < previous) {
                                    label.addStyleName("pushNegative");
                                }
                                userView.getUI().push(); //If price same for second time in a row we dont need to push
                            });
                            try {
                                Thread.sleep(1000); //TODO Gahhhh...
                            } catch (InterruptedException e1) {
                            }
                            userView.getUI().access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                                label.removeStyleName("pushPositive");
                                label.removeStyleName("pushNegative");
                                userView.getUI().push();
                            });
                        });
                        final Registration oldRegistration = registrations.put(position.getCurrencyPair(), registration);
                        if(oldRegistration != null) {
                            oldRegistration.cancel();
                        }
                        return label;
                    }
                })
                .withGeneratedColumn("Time", entity -> {
                    Optional<Price> lastPrice = userView.priceService.getLatestPriceForCurrencyPair(entity.getCurrencyPair());
                    if (lastPrice.isPresent()) {
                        return lastPrice.get().time.truncatedTo(ChronoUnit.SECONDS);
                    }
                    return "n/a";
                })
                .withFullWidth();
        positionsTable.setColumnExpandRatio("depotName", 0.1f);
        positionsTable.setSelectable(true);
        positionsTable.addActionHandler(new Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                if (target != null) {
                    return new Action[] { CLOSE_POSITION_ACTION };
                }
                return new Action[] {};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                final UIPosition position = (UIPosition) target;
                ConfirmDialog.show(userView.getUI(), "Really close position?",
                        "Are you really sure you want to close your " + position.getCurrencyPair() + " position?", "Yes", "No", dialog -> {
                    if (dialog.isConfirmed()) {
                        try {
                            ClosePositionResponse response = userView.brokerConnection.closePosition(Integer.valueOf(position.getBrokerDepotId()), position.getCurrencyPair());
                            depotService.syncDepot(position.depot);
                            deregister(position.getCurrencyPair());
                            listEntities(); //TODO do this async. Publish/subscribe on updated depot events
                            LOG.info("Position closed {}, {}", sender, target);
                            Notification.show("Your position in " + response.currencyPair + " was closed to a price of " + response.price, WARNING_MESSAGE);
                        } catch (TradingHaltedException e) {
                            Notification.show("Sorry, unable to close the position since the trading is currently halted", ERROR_MESSAGE);
                        } catch (Exception e) {
                            LOG.warn("Exception when closing position for position {}", position);
                            Notification.show("Sorry, unable to close the position due to " + e.getMessage(), ERROR_MESSAGE);
                        }
                    }
                });
            }
        });
        listEntities();
        tab.addComponents(positionsTable);
        tab.expand(positionsTable);
        return tab;
    }

    private void listEntities() {
        List<UIPosition> allPositions = new ArrayList<>();
        for (DbDepot depot : depotService.findByUserId(user.getId())) {
            allPositions.addAll(depot.getPositions().stream().map(p -> new UIPosition(depot, p)).collect(Collectors.toList()));
        }
        positionsTable.setBeans(allPositions);

    }

    //Used to clean up the eventbus registrations we create
    public void deregisterAll() {
        final Collection<Registration> r = registrations.values();
        if (r != null) {
            LOG.info("About to deregisterAll {} registrations", this.registrations.size());
            r.forEach(Registration::cancel);
        } else {
            LOG.info("No registrations found");
        }
        registrations.clear();
    }

    private final void deregister(CurrencyPair currencyPair) {
        if(!registrations.containsKey(currencyPair)) return;
        final Registration registration = registrations.get(currencyPair);
        if(registration != null) {
            registration.cancel();
        }
    }
}
