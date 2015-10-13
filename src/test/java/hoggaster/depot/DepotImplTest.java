package hoggaster.depot;

import hoggaster.domain.OrderService;
import hoggaster.domain.brokers.Broker;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by svante2 on 2015-10-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class DepotImplTest extends TestCase {

    private Depot depot;

    @Mock
    private OrderService orderService;

    @Mock
    private DepotService depotService;

    public void setUp() throws Exception {
        //DbDepot newDepot = new DbDepot(user.getId(), name, broker, brokerDepot.name, brokerId, brokerDepot.marginRate, brokerDepot.currency, brokerDepot.balance, brokerDepot.unrealizedPl, brokerDepot.realizedPl, brokerDepot.marginUsed, brokerDepot.marginAvail, brokerDepot.openTrades, brokerDepot.openOrders, Instant.now());
        DbDepot dbDepot = new DbDepot("USER_ID", "Test dbDepot", Broker.OANDA, "Primary ", "9678914", new BigDecimal(0.05), "USD", new BigDecimal(0.0), new BigDecimal(0.0), new BigDecimal(0.0), new BigDecimal(0.0), new BigDecimal(1000.0), 0, 0, Instant.now(), true, DbDepot.Type.DEMO);
        depot = new DepotImpl(dbDepot.getId(), orderService, depotService);


    }

    @Test
    public void testSell() throws Exception {

    }

    @Test
    public void testBuy() throws Exception {

    }
}