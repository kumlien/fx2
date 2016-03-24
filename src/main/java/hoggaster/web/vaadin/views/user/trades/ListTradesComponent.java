package hoggaster.web.vaadin.views.user.trades;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderServiceImpl;
import hoggaster.domain.prices.Price;
import hoggaster.domain.trades.CloseTradeResponse;
import hoggaster.domain.trades.Trade;
import hoggaster.domain.trades.TradeService;
import hoggaster.oanda.exceptions.TradingHaltedException;
import hoggaster.web.vaadin.GuiUtils;
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
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static java.util.stream.Collectors.toList;
import static reactor.bus.selector.Selectors.$;

/**
 * Used to display a list of all active trades for a user.
 * <p>
 * Created by svante.kumlien on 01.03.16.
 */
@Component
@ViewScope
public class ListTradesComponent implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ListTradesComponent.class);

    private static final Action CLOSE_TRADE_ACTION = new Action("Close this trade");

    private static final Action OPEN_TRADE_ACTION = new Action("Open trade");

    private static final Action EDIT_TRADE_ACTION = new Action("Edit this trade");

    private final DepotService depotService;

    private final TradeService tradeService;

    private final OrderServiceImpl orderService;

    private final Map<Long, Registration> registrations = new ConcurrentHashMap<>();

    public final EventBus priceEventBus;

    private final EventBus depotEventBus;

    private MTable<UITrade> tradesTable;

    private FormUser user;

    private Map<Trade, Long> lastReceivedPricePerTrade = new ConcurrentHashMap<>();

    //Read this once
    private Collection<DbDepot> userDepots;

    @Autowired
    public ListTradesComponent(DepotService depotService, TradeService tradeService, OrderServiceImpl orderService, @Qualifier("priceEventBus") EventBus priceEventBus, @Qualifier("depotEventBus")EventBus depotEventBus) {
        this.depotService = depotService;
        this.tradeService = tradeService;
        this.orderService = orderService;
        this.priceEventBus = priceEventBus;
        this.depotEventBus = depotEventBus;
    }

    //Create the tab with the (active) trades
    public MVerticalLayout setUp(FormUser user, UserView parentView) {
        this.user = user;
        userDepots = depotService.findByUserId(user.getId());

        userDepots.forEach(d -> {
            depotEventBus.on($(d.getId()), e -> {
                LOG.info("Depot {} is updated, refresh list with trades", e.getData());
                populateTradeListFromDb();
            });
        });

        final String defaultPriceLabel = "Waiting for prices...";
        MVerticalLayout tab = new MVerticalLayout();
        tradesTable = new MTable<>(UITrade.class)
                .withProperties("instrument", "side", "openPrice", "openTime", "units")
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
                            Long lastUpdate = lastReceivedPricePerTrade.get(trade.trade);
                            if (lastUpdate == null || System.currentTimeMillis() - lastUpdate > 2500) {
                                Double current = ((Price) e.getData()).ask.doubleValue();
                                Double previous = Double.valueOf(label.getValue().equals(defaultPriceLabel) ? current.toString() : label.getValue());
                                LOG.info("Got a new price: {}", e.getData());
                                GuiUtils.setAndPushDoubleLabel(parentView.getUI(), label, current, previous);
                                lastReceivedPricePerTrade.put(trade.trade, System.currentTimeMillis());
                            }
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
                    return new Action[]{EDIT_TRADE_ACTION, CLOSE_TRADE_ACTION};
                }
                return new Action[]{OPEN_TRADE_ACTION};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                final UITrade trade = (UITrade) target;
                if (action == CLOSE_TRADE_ACTION) {
                    ConfirmDialog.show(parentView.getUI(), "Really close trade?", "Are you really sure you want to close your trade?", "Yes", "No", dialog -> {
                        if (dialog.isConfirmed()) {
                            try {
                                CloseTradeResponse response = tradeService.closeTrade(trade.trade, trade.depot.getBrokerId());
                                deregister(trade.trade);
                                populateTradeListFromDb(); //TODO do this async. Publish/subscribe on updated trades events
                                LOG.debug("trade closed {}, {}", sender, target);
                                Notification.show("Your trade was closed to a price of " + response.price, WARNING_MESSAGE);
                            } catch (TradingHaltedException e) {
                                Notification.show("Sorry, unable to close the position since the trading is currently halted", ERROR_MESSAGE);
                            } catch (Exception e) {
                                LOG.warn("Exception when closing trade {}", trade, e);
                                Notification.show("Sorry, unable to close the position due to " + e.getMessage(), ERROR_MESSAGE);
                            }
                        }
                    });
                } else if (action == OPEN_TRADE_ACTION) {
                    TradeForm tradeForm = new TradeForm(priceEventBus, userDepots);
                    final Window tradeFormWindow = tradeForm.openInModalPopup();
                    tradeFormWindow.setCaption("Open new market trade");
                    tradeForm.setSavedHandler(t -> sendOrderForNewTrade(t, tradeFormWindow));
                    tradeForm.setResetHandler(t -> {
                        LOG.info("Trade: " + t);
                        UI.getCurrent().removeWindow(tradeFormWindow);
                    });
                }
            }
        });

        populateTradeListFromDb();
        tab.addComponents(tradesTable);
        tab.expand(tradesTable);
        return tab;
    }

    //Send a request to open a new trade
    private void sendOrderForNewTrade(TradeForm.FormTrade formTrade, Window tradeFormWindow) {
        OrderRequest request = OrderRequest.Builder.anOrderRequest()
                .withCurrencyPair(formTrade.getInstrument())
                .withExternalDepotId(formTrade.getDepot().brokerId)
                .withSide(formTrade.getSide())
                .withType(formTrade.getOrderType())
                .withUnits(formTrade.getUnits())
                .build();
        OrderResponse orderResponse = orderService.sendOrder(request);
        UI.getCurrent().removeWindow(tradeFormWindow);
        if (orderResponse.tradeWasOpened()) {
            final Trade trade = orderResponse.getOpenedTrade(null, null).get();
            Notification.show("Trade opened for " + trade.getUnits() + " units of " + trade.getInstrument() + " to price " + trade.getOpenPrice(), WARNING_MESSAGE);
        } else {
            Notification.show("No trade opened " + orderResponse.toString(), WARNING_MESSAGE);
        }
        depotService.syncDepotAsync(formTrade.getDepot());
    }

    //Read all depots from db and their open trades.
    private void populateTradeListFromDb() {
        List<UITrade> allTrades = new ArrayList<>();
        for (DbDepot depot : depotService.findByUserId(user.getId())) {
            allTrades.addAll(depot.getOpenTrades().stream().map(t -> new UITrade(depot, t)).collect(toList()));
        }
        allTrades.forEach(t -> {
            if(tradesTable.containsId(t)) {
                LOG.info("The table already contains trade with id {}", t.getBrokerId());
            } else {
                LOG.info("No trade with id {} in the table", t.getBrokerId());
            }
        });
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
        does this work when closing a trade??
        final Registration registration = registrations.get(trade.brokerId);
        if (registration != null) {
            registration.cancel();
            registrations.remove(trade.brokerId);
        }
    }
}
