package hoggaster.depot;

import hoggaster.domain.Instrument;
import hoggaster.domain.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to handle logic for a depot.
 * Not so found of this approach, splitting persistent stuff and business logic
 *
 * Created by svante2 on 2015-10-11.
 */
public class DepotImpl implements Depot {


    private static final Logger LOG = LoggerFactory.getLogger(DepotImpl.class);

    private final DbDepot dbDepot;

    // The service we use to deal with orders
    private final OrderService orderService;

    public DepotImpl(DbDepot dbDepot, OrderService orderService) {
        this.dbDepot = dbDepot;
        this.orderService = orderService;
    }


    @Override
    public void sell(Instrument instrument, String robotId) {
        LOG.info("We are told by robot {} to sell {}", instrument);
    }

    @Override
//    New positions should not push available margin below 50%. If available margin is below 50% no new positions can be opened.
//    Other details on position sizing is handled by robot.
    public void buy(Instrument instrument, String robotId) {
        LOG.info("We are told by robot {} to buy {}", instrument);
    }
}
