package hoggaster.web.vaadin.views.user.trades;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.prices.Price;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.oanda.exceptions.TradingHaltedException;
import hoggaster.web.vaadin.views.user.UserForm.FormUser;
import hoggaster.web.vaadin.views.user.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTable.SimpleColumnGenerator;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.Environment;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static reactor.bus.selector.Selectors.$;

/**
 * Used to display a list of all active trades for a user.
 *
 * Created by svante.kumlien on 01.03.16.
 */
@Component
@ViewScope
public class ListTradesComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListTradesComponent.class);

    private static final Action CLOSE_TRADE_ACTION = new Action("Close this trade");

    private static final Action EDIT_TRADE_ACTION = new Action("Edit this trade");

    private final DepotService depotService;

    private final Map<Long, Registration> registrations = new ConcurrentHashMap<>();

    public final EventBus priceEventBus;

    private MTable<UITrade> tradesTable;

    private FormUser user;

    @Autowired
    public ListTradesComponent(DepotService depotService, @Qualifier("priceEventBus") EventBus priceEventBus) {
        this.depotService = depotService;
        this.priceEventBus = priceEventBus;
    }

    //Create the tab with the current active trades
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;
        final String defaultPriceLabel = "Fetching...";
        MVerticalLayout tab = new MVerticalLayout();
        tradesTable = new MTable<>(UITrade.class)
                .withProperties("instrument", "side","openPrice", "openTime", "units")
                .withColumnHeaders("Currency pair", "Side", "Open price", "Open time", "Quantity")
                .withGeneratedColumn("Current price", new SimpleColumnGenerator<UITrade>() {
                    @Override
                    public Object generate(UITrade trade) {
                        Label label = new Label(defaultPriceLabel);
                        label.addStyleName("pushbox");
                        LOG.info("Creating new registration for trade {}", trade.getInstrument());
                        final Registration registration = priceEventBus.on($("prices." + trade.getInstrument()), e -> {
                            if (parentView.getUI() == null) { //Continue to push until gui is gone
                                deregisterAll();
                                return;
                            }

                            Double current = ((Price) e.getData()).ask.doubleValue();
                            Double previous = Double.valueOf(label.getValue().equals(defaultPriceLabel) ? current.toString() : label.getValue());
                            LOG.info("Got a new price: {}", e.getData());
                            parentView.getUI().access(() -> {
                                label.setValue(current.toString());
                                label.removeStyleName("pushPositive");
                                label.removeStyleName("pushNegative");
                                if (current > previous) {
                                    label.addStyleName("pushPositive");
                                } else if (current < previous) {
                                    label.addStyleName("pushNegative");
                                }
                                parentView.getUI().push(); //If price same for second time in a row we dont need to push
                            });

                            Environment.get().getTimer().submit(l -> {
                                parentView.getUI().access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                                    label.removeStyleName("pushPositive");
                                    label.removeStyleName("pushNegative");
                                    parentView.getUI().push();
                                });
                            },1, SECONDS);
                        });
                        deregister(trade.trade); //cancel any existing registrations for this instrument
                        registrations.put(trade.trade.brokerId, registration);
                        return label;
                    }
                })
                .setSortableProperties("instrument")
                .withFullWidth();
        tradesTable.expandFirstColumn();
        tradesTable.setSelectable(true);
        tradesTable.addActionHandler(new Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                if (target != null) {
                    return new Action[] { EDIT_TRADE_ACTION, CLOSE_TRADE_ACTION };
                }
                return new Action[] {};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                final UITrade trade = (UITrade) target;
                ConfirmDialog.show(parentView.getUI(), "Really close trade?",
                        "Are you really sure you want to close your trade?", "Yes", "No", dialog -> {
                    if (dialog.isConfirmed()) {
                        try {
                            CloseTradeResponse response = parentView.brokerConnection.closeTrade(trade.trade, trade.depot.getBrokerId());
                            depotService.syncDepot(trade.depot);
                            deregister(trade.trade);
                            listEntities(); //TODO do this async. Publish/subscribe on updated trades events
                            LOG.info("trade closed {}, {}", sender, target);
                            Notification.show("Your trade was closed to a price of " + response.price, WARNING_MESSAGE);
                        } catch (TradingHaltedException e) {
                            Notification.show("Sorry, unable to close the position since the trading is currently halted", ERROR_MESSAGE);
                        } catch (Exception e) {
                            LOG.warn("Exception when closing trade {}", trade);
                            Notification.show("Sorry, unable to close the position due to " + e.getMessage(), ERROR_MESSAGE);
                        }
                    }
                });
            }
        });
        listEntities();
        tab.addComponents(tradesTable);
        tab.expand(tradesTable);
        return tab;
    }

    private void listEntities() {
        List<UITrade> allTrades = new ArrayList<>();
        for (DbDepot depot : depotService.findByUserId(user.getId())) {
            allTrades.addAll(depot.getOpenTrades().stream().map(t -> new UITrade(depot, t)).collect(Collectors.toList()));
        }
        tradesTable.setBeans(allTrades);
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

    private final void deregister(Trade trade) {
        final Registration registration = registrations.get(trade.brokerId);
        if(registration != null) {
            registration.cancel();
            registrations.remove(trade.brokerId);
        }
    }
}
