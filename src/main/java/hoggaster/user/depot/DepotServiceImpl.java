package hoggaster.user.depot;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.domain.Broker;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.BrokerDepot;
import hoggaster.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Created by svante on 15-09-22.
 */
@Service
public class DepotServiceImpl implements  DepotService {

    private static final Logger LOG = LoggerFactory.getLogger(DepotServiceImpl.class);

    private final DepotRepo depotRepo;

    private final BrokerConnection brokerConnection;

    @Autowired
    public DepotServiceImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.depotRepo = depotRepo;
        this.brokerConnection = brokerConnection;
    }


    @Override
    //String userId, String name, Broker broker, String brokerDepotName, String brokerId, BigDecimal marginRate, String currency, BigDecimal balance, BigDecimal unrealizedPl, BigDecimal realizedPl, BigDecimal marginUsed, BigDecimal marginAvailable, Integer openTrades, Integer openOrders, Instant lastSynchronizedWithBroker) {
    public Depot createDepot(User user, String name, Broker broker, String brokerId) {
        Preconditions.checkArgument(broker == Broker.OANDA, "Sorry, we only support " + Broker.OANDA + " at the moment");

        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "The depot must have a name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(brokerId), "The depot must have a name");
        Preconditions.checkArgument(broker != null, "The broker must be specified");

        Preconditions.checkArgument(depotRepo.findBybrokerId(brokerId) == null, "There is already a depot connected with broker id '" + brokerId + "'");
        BrokerDepot brokerDepot = brokerConnection.getDepot(brokerId);
        Preconditions.checkArgument(brokerDepot != null);

        Depot newDepot = new Depot(user.getId(), name, broker, brokerDepot.name, brokerId, brokerDepot.marginRate, brokerDepot.currency, brokerDepot.balance, brokerDepot.unrealizedPl, brokerDepot.realizedPl, brokerDepot.marginUsed, brokerDepot.marginAvail, brokerDepot.openTrades, brokerDepot.openOrders, Instant.now());
        newDepot = depotRepo.save(newDepot);
        return newDepot;
    }


    @Override
    public void deleteDepot(Depot depot) {
        //TODO Synch with broker and check transactions/orders/balance etc
        LOG.info("Deleting depot {}", depot);
        depotRepo.delete(depot);
    }

    @Override
    public Depot findDepotById(String id) {
        return depotRepo.findOne(id);
    }
}
