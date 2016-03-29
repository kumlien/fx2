package hoggaster.web.vaadin.views.user.trades;

import com.google.common.base.Preconditions;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderServiceImpl;
import hoggaster.domain.orders.OrderSide;
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
import reactor.fn.tuple.Tuple2;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static hoggaster.domain.orders.OrderRequest.Builder.anOrderRequest;
import static java.util.stream.Collectors.toList;
import static reactor.bus.selector.Selectors.$;

/**
 * Used to display a list of all active trades for a user. Listens to the depot eventbus and refreshes the list
 * with trades when a depot change is received.
 *
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

    final String defaultPriceLabel = "Waiting for prices...";

    private final DepotService depotService;

    private final TradeService tradeService;

    private final OrderServiceImpl orderService;

    private final Map<Long, Registration> registrations = new ConcurrentHashMap<>();

    public final EventBus priceEventBus;

    private final EventBus depotEventBus;

    private MTable<UITrade> tradesTable;

    private FormUser user;

    //move this into the pusher
    private Map<Trade, Long> lastReceivedPricePerTrade = new ConcurrentHashMap<>();

    //Read this once
    private Collection<DbDepot> userDepots;

    //Map with pushers, one for each trade/row: tradeId/Pusher as key value
    Map<Long, Pusher> pushers = new ConcurrentHashMap<>();

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

        MVerticalLayout tab = new MVerticalLayout();
        tradesTable = new MTable<>(UITrade.class)
                .withProperties("instrument", "side", "openPrice", "openTime", "units")
                .withColumnHeaders("Currency pair", "Side", "Open price", "Open time", "Units")
                .withGeneratedColumn("Current bid", new SimpleColumnGenerator<UITrade>() {
                    @Override
                    public Object generate(UITrade trade) {//Generate a column with the 'price'. Create and return a label which we can push new prices to later.
                        Pusher p = getPusherForTrade(trade, parentView);
                        p.start();
                        return p.createBidLabel();
                    }
                })
                .withGeneratedColumn("Current ask", new SimpleColumnGenerator<UITrade>() {
                    @Override
                    public Object generate(UITrade trade) {//Generate a column with the 'price'. Create and return a label which we can push new prices to later.
                        Pusher p = getPusherForTrade(trade, parentView);
                        p.start();
                        return p.createAskLabel();
                    }
                })
                .withGeneratedColumn("Profit/Loss", new SimpleColumnGenerator<UITrade>() {
                    @Override
                    public Object generate(UITrade trade) {//Generate a column with the 'price'. Create and return a label which we can push new prices to later.
                        Pusher p = getPusherForTrade(trade, parentView);
                        p.start();
                        return p.createProfitLossLabel();
                    }
                })
                .setSortableProperties("instrument", "side")
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
                    handleCloseTrade(trade, parentView);
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

    private Pusher getPusherForTrade(UITrade trade, UserView parentView) {
        Preconditions.checkArgument(trade.trade.getBrokerId() != null, "No brokerId set on the trade!");
        Pusher pusher = pushers.getOrDefault(trade.trade.getBrokerId(), new Pusher(trade.trade, parentView));
        pushers.put(trade.trade.getBrokerId(), pusher);
        return pusher;
    }

    private void handleCloseTrade(UITrade trade, UserView parentView) {
        ConfirmDialog.show(parentView.getUI(), "Really close trade?", "Are you really sure you want to close your trade?", "Yes", "No", dialog -> {
            if (dialog.isConfirmed()) {
                try {
                    CloseTradeResponse response = tradeService.closeTrade(trade.trade, trade.depot.getBrokerId());
                    deregister(trade.trade);
                    populateTradeListFromDb(); //TODO do this async. Publish/subscribe on updated trades events
                    String profitOrLoss = response.profit.compareTo(BigDecimal.ZERO) > 0 ? "profit" : "loss";
                    Notification.show("Your trade was closed to a price of " + response.price + ". The " + profitOrLoss + " was " + response.profit, WARNING_MESSAGE);
                } catch (TradingHaltedException e) {
                    Notification.show("Sorry, unable to close the position since the trading is currently halted", ERROR_MESSAGE);
                } catch (Exception e) {
                    LOG.warn("Exception when closing trade {}", trade, e);
                    Notification.show("Sorry, unable to close the position due to " + e.getMessage(), ERROR_MESSAGE);
                }
            }
        });
    }

    //Send a request to open a new trade
    private void sendOrderForNewTrade(TradeForm.FormTrade formTrade, Window tradeFormWindow) {
        OrderRequest request = anOrderRequest()
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
            LOG.info("Trade received from oanda: {}", trade);
            UITrade newTrade = new UITrade(formTrade.getDepot(), trade);
            tradesTable.addBeans(newTrade);
            UI.getCurrent().access(UI.getCurrent()::push);
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
                tradesTable.addBeans(t);
            }
        });
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
        //does this work when closing a trade??
        final Registration registration = registrations.remove(trade.brokerId);
        if (registration != null) {
            registration.cancel();
        }
    }


    //Class used to push updates for a row in the table.
    private class Pusher {

        static final String DEFAULT_LABEL = "waiting...";

        final Trade trade;
        final UserView parentView;
        final CurrencyPair instrument;
        Registration registration;
        Boolean started = false;
        Long lastPush = System.currentTimeMillis();

        private Label askLabel;
        private Label bidLabel;
        private Label profitLossLabel;

        private Pusher(Trade trade, UserView parentView) {
            this.trade = trade;
            this.parentView = parentView;
            this.instrument = trade.instrument;
        }

        Label createAskLabel() {
            Label label = new Label(DEFAULT_LABEL);
            label.addStyleName("pushbox");
            askLabel = label;
            return label;
        }

        Label createBidLabel() {
            Label label = new Label(DEFAULT_LABEL);
            label.addStyleName("pushbox");
            bidLabel = label;
            return label;
        }

        Label createProfitLossLabel() {
            Label label = new Label(DEFAULT_LABEL);
            label.addStyleName("pushbox");
            profitLossLabel = label;
            return label;
        }

        void start() {
            synchronized (started) {
                if (started) return;
                started = true;
            }
            registration = priceEventBus.on($("prices." + instrument), e -> {
                LOG.debug("Got a new price: {}", e.getData());
                if (parentView.getUI() == null) { //Continue to push until gui is gone
                    stop();
                    return;
                }
                Map<Label, Tuple2<Double, Double>> values = new HashMap<>();
                if (System.currentTimeMillis() - lastPush > 5000) {
                    lastPush = System.currentTimeMillis();
                    try {
                        if (askLabel != null) {
                            Double currentAsk = ((Price) e.getData()).ask.doubleValue();
                            Number lastAsk = GuiUtils.df.parse(askLabel.getValue().equals(DEFAULT_LABEL) ? currentAsk.toString() : askLabel.getValue());
                            values.put(askLabel, Tuple2.of(currentAsk, lastAsk.doubleValue()));
                        }
                        if (bidLabel != null) {
                            Double currentBid = ((Price) e.getData()).bid.doubleValue();
                            Double lastBid = GuiUtils.df.parse(bidLabel.getValue().equals(DEFAULT_LABEL) ? currentBid.toString() : bidLabel.getValue()).doubleValue();
                            values.put(bidLabel, Tuple2.of(currentBid, lastBid));
                        }
                        if (profitLossLabel != null) {
                            BigDecimal currentPrice = trade.side == OrderSide.buy ? ((Price) e.getData()).bid : ((Price) e.getData()).ask;
                            Double currentPL = currentPrice.subtract(trade.openPrice).multiply(trade.getUnits()).divide(currentPrice, MathContext.DECIMAL32).doubleValue();
                            Double lastPL = GuiUtils.df.parse(profitLossLabel.getValue().equals(DEFAULT_LABEL) ? currentPL.toString() : profitLossLabel.getValue()).doubleValue();
                            values.put(profitLossLabel, Tuple2.of(currentPL, lastPL));
                        }
                        GuiUtils.setAndPushDoubleLabels(parentView.getUI(), values);
                    } catch (ParseException p) {
                        LOG.error("Dohh", p);
                    }
                }
            });
            LOG.info("Registration for trade {} created", trade.getId());
        }

        void stop() {
            LOG.info("Stopping and cancel registration for trade {} ", trade.getId());
            registration.cancel();
        }

    }
}
