package hoggaster.web.vaadin.views.user.trades;

import com.google.common.base.Preconditions;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
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
import reactor.fn.tuple.Tuple3;
import rx.schedulers.Schedulers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static hoggaster.domain.orders.OrderRequest.Builder.anOrderRequest;
import static java.util.stream.Collectors.toList;
import static reactor.bus.selector.Selectors.$;

/**
 * Used to display a list of all active trades for a user. Listens to the depot eventbus and refreshes the list
 * with trades when a depot change is received.
 * <p>
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

    public final EventBus priceEventBus;

    private final EventBus depotEventBus;

    private MTable<UITrade> tradesTable;

    private FormUser user;

    //Read this once
    private Collection<DbDepot> userDepots;

    //Map with pushers, one for each trade/row: tradeId/Pusher as key value
    Map<Long, Pusher> pushers = new ConcurrentHashMap<>();

    @Autowired
    public ListTradesComponent(DepotService depotService, TradeService tradeService, OrderServiceImpl orderService, @Qualifier("priceEventBus") EventBus priceEventBus, @Qualifier("depotEventBus") EventBus depotEventBus) {
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
                .withGeneratedColumn("Current bid", trade -> {//Generate a column with the 'price'. Create and return a label which we can push new prices to later.
                    Pusher p = getPusherForTrade(trade, parentView);
                    p.start();
                    return p.createBidLabel();
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
                .withGeneratedColumn("Spread (%)", new SimpleColumnGenerator<UITrade>() {
                    @Override
                    public Object generate(UITrade trade) {
                        Pusher p = getPusherForTrade(trade, parentView);
                        p.start();
                        return p.createSpreadLabel();
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
                    if(userDepots.isEmpty()) {
                        Notification.show("You have no depot connected. It's not possible to open a trade without a connected depot", ERROR_MESSAGE);
                        return;
                    }
                    TradeForm tradeForm = new TradeForm(priceEventBus, userDepots);
                    final Window tradeFormWindow = tradeForm.openInModalPopup();
                    tradeFormWindow.setCaption("Open new market trade");
                    tradeForm.setSavedHandler(t -> sendOrderForNewTrade(t, tradeFormWindow));
                    tradeForm.setResetHandler(t -> {
                        UI.getCurrent().removeWindow(tradeFormWindow);
                    });
                } else if (action == EDIT_TRADE_ACTION) {
                    Notification.show("Not implemented...", WARNING_MESSAGE);
                }
            }
        });

        populateTradeListFromDb();
        tab.addComponents(tradesTable);
        tab.expand(tradesTable);
        return tab;
    }

    //Used to clean up the eventbus registrations we create
    public void deregisterAll() {
        pushers.values().forEach(Pusher::stop);
        pushers.clear();
    }

    private final void deregister(Trade trade) {
        final Pusher pusher = pushers.get(trade.brokerId);
        if (pusher != null) pusher.stop();
        pushers.remove(trade.brokerId);
    }

    private Pusher getPusherForTrade(UITrade trade, UserView parentView) {
        Preconditions.checkArgument(trade.trade.getBrokerId() != null, "No brokerId set on the trade!");
        Pusher pusher = pushers.getOrDefault(trade.trade.getBrokerId(), new Pusher(trade.trade, parentView));
        pushers.put(trade.trade.getBrokerId(), pusher);
        LOG.info("Number of pushers: {}", pushers.size());
        return pusher;
    }

    private void handleCloseTrade(UITrade trade, UserView parentView) {
        ConfirmDialog.show(parentView.getUI(), "Really close trade?", "Are you really sure you want to close your trade?", "Yes", "No", dialog -> {
            if (dialog.isConfirmed()) {
                try {
                    CloseTradeResponse response = tradeService.closeTrade(trade.trade, trade.depot.getBrokerId());
                    depotService.syncDepot(trade.depot);
                    populateTradeListFromDb();
                    String profitOrLoss = response.profit.compareTo(BigDecimal.ZERO) > 0 ? "profit" : "loss";
                    deregister(trade.trade);
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
            if (tradesTable.containsId(t)) {
                LOG.info("The table already contains trade with id {}", t.getBrokerId());
            } else {
                LOG.info("No trade with id {} in the table", t.getBrokerId());
                tradesTable.addBeans(t);
            }
        });
    }

    //Class used to push updates for a row in the table.
    private class Pusher {

        static final String DEFAULT_LABEL = "waiting...";

        final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

        final long MIN_TIME_BETWEEN_PUSH = 5000l;

        final Trade trade;
        final UserView parentView;
        final CurrencyPair instrument;
        Registration registration;
        Boolean started = false;

        private Label askLabel;
        private Label bidLabel;
        private Label profitLossLabel;
        private Label spreadLabel;

        private Pusher(Trade trade, UserView parentView) {
            this.trade = trade;
            this.parentView = parentView;
            this.instrument = trade.instrument;
        }

        Label createAskLabel() {
            askLabel= createDefaultLabel();
            return askLabel;
        }

        Label createBidLabel() {
            bidLabel = createDefaultLabel();
            return bidLabel;
        }

        Label createProfitLossLabel() {
            profitLossLabel = createDefaultLabel();
            return profitLossLabel;
        }

        Label createSpreadLabel() {
            spreadLabel = createDefaultLabel();
            return spreadLabel;
        }

        void start() {
            synchronized (started) {
                if (started) return;
                started = true;
            }


            rx.Observable.create(p -> {
                LOG.info("Creating **new** registration for position {}", instrument);
                registration = priceEventBus.on($("prices." + instrument), e -> {
                    if (parentView.getUI() == null) { //Continue to push until gui is gone
                        deregisterAll();
                        return;
                    }
                    LOG.debug("Got a price, putting it in the stream: {}", e.getData());
                    p.onNext(e.getData());
                });
            })
                    .subscribeOn(Schedulers.io())
                    .sample(5000, TimeUnit.MILLISECONDS)
                    .subscribe(price -> {
                        Price tick = (Price) price;
                        Map<Label, Tuple3<Double, Double, Boolean>> values = new HashMap<>();
                        try {
                            if (askLabel != null) {
                                Double currentAsk = tick.ask.doubleValue();
                                LOG.debug("Value of askLabel: {}", askLabel.getValue());
                                Double lastAsk = askLabel.getValue().equals(DEFAULT_LABEL) ? currentAsk : GuiUtils.df.parse(askLabel.getValue()).doubleValue();
                                values.put(askLabel, Tuple2.of(currentAsk, lastAsk.doubleValue(), false));
                            }
                            if (bidLabel != null) {
                                Double currentBid = tick.bid.doubleValue();
                                LOG.debug("Value of bidLabel: {}", bidLabel.getValue());
                                Double lastBid = bidLabel.getValue().equals(DEFAULT_LABEL) ? currentBid : GuiUtils.df.parse(bidLabel.getValue()).doubleValue();
                                values.put(bidLabel, Tuple2.of(currentBid, lastBid, false));
                            }
                            if (profitLossLabel != null) {
                                BigDecimal currentPrice = trade.side == OrderSide.buy ? tick.bid : tick.ask;
                                String currentLabelValue = profitLossLabel.getValue();
                                LOG.debug("Value of PLLabel: {}", currentLabelValue);
                                Double currentPL = currentPrice.subtract(trade.openPrice).multiply(trade.getUnits()).divide(currentPrice, MathContext.DECIMAL32).doubleValue();
                                Double lastPL = currentLabelValue.equals(DEFAULT_LABEL) ? currentPL : GuiUtils.df.parse(currentLabelValue).doubleValue();
                                values.put(profitLossLabel, Tuple2.of(currentPL, lastPL, true));
                            }
                            if (spreadLabel != null) {
                                Double currentSpread = tick.ask.subtract(tick.bid).divide(tick.ask, MathContext.DECIMAL32).multiply(ONE_HUNDRED).doubleValue();
                                LOG.debug("Value of spreadLabel: {}", spreadLabel.getValue());
                                Double lastSpread = spreadLabel.getValue().equals(DEFAULT_LABEL) ? currentSpread : GuiUtils.df.parse(spreadLabel.getValue()).doubleValue();
                                values.put(spreadLabel, Tuple2.of(currentSpread, lastSpread, false));
                            }
                            GuiUtils.setAndPushDoubleLabels(parentView.getUI(), values);
                        } catch (UIDetachedException ude) {
                            LOG.info("Ui is detached...");
                            stop();
                        } catch (Exception p) {
                            LOG.error("Dohh for price {}", tick, p);
                        }
                    });
        }

        void stop() {
            if(!registration.isCancelled()) {
                LOG.info("Stopping and cancel registration for trade {} ({})", trade.getBrokerId(), trade.instrument);
                registration.cancel();
            }
        }

        private final Label createDefaultLabel() {
            Label label = new Label(DEFAULT_LABEL);
            label.addStyleName("pushbox");
            return label;
        }
    }
}
