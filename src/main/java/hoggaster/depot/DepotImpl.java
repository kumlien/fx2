package hoggaster.depot;

import hoggaster.domain.Instrument;
import hoggaster.domain.MarketUpdate;
import hoggaster.domain.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.oanda.responses.OandaOrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Used to handle logic for a depot.
 * Not so found of this approach, splitting persistent stuff and business logic
 *
 * Created by svante2 on 2015-10-11.
 */
public class DepotImpl implements Depot {


    private static final Logger LOG = LoggerFactory.getLogger(DepotImpl.class);

    private final String dbDepotId;

    private final DepotService depotService;

    // The service we use to deal with orders
    private final OrderService orderService;

    public DepotImpl(String dbDepotId, OrderService orderService, DepotService depotService) {
        Objects.requireNonNull(depotService.findDepotById(dbDepotId), "Unable to find a depot with id '" + dbDepotId + "'");
        this.depotService = depotService;
        this.dbDepotId = dbDepotId;
        this.orderService = orderService;
    }


    @Override
    public void sell(Instrument instrument, int requestedUnits, String robotId) {
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        LOG.info("We are told by robot {} to sell {}",robotId, instrument);
        if (!dbDepot.ownThisInstrument(instrument)) {
            LOG.info("Nahh, we don't own {} yet...", instrument.name());
            return;
        }

        LOG.info("Ooops, we should sell what we got of {}!", instrument.name());
    }

    @Override
//    New positions should not push available margin below 50%. If available margin is below 50% no new positions can be opened.
//    Other details on position sizing is handled by robot.
    public void buy(Instrument instrument, int requestedUnits, MarketUpdate marketUpdate, String robotId) {
        LOG.info("We are told by robot {} to buy {}",robotId, instrument);
        DbDepot dbDepot = depotService.findDepotById(dbDepotId);
        if (dbDepot.ownThisInstrument(instrument)) {
            LOG.info("Nahh, we already own {}, only buy once...", instrument.name());
            return;
        }

        //TODO Check for margin below 50%
        BigDecimal marginAvailable = dbDepot.getMarginAvailable();
        final BigDecimal maxAmountToBuyFor = marginAvailable.multiply(new BigDecimal(0.02));

        //TODO For now we try to buy for 2% of depot value
        BigDecimal balance = dbDepot.getBalance();


        LOG.info("Ooops, we should buy since we don't own any {} yet!", instrument.name());
        OrderRequest order = new OrderRequest(dbDepot.getBrokerId(), instrument, 1000L, OrderSide.buy, OrderType.market, null, null);
        OandaOrderResponse response = orderService.sendOrder(order);
        LOG.info("Order away and we got an response! {}", response);
    }
}
