package hoggaster.domain.depots;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Created by svante on 15-09-22.
 */
@Service
public class DepotServiceImpl implements  DepotService {

    private static final Logger LOG = LoggerFactory.getLogger(DepotServiceImpl.class);

    private final DepotRepo depotRepo;

    //Only support one broker connection for now (oanda)
    private final BrokerConnection brokerConnection;

    @Autowired
    public DepotServiceImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.depotRepo = depotRepo;
        this.brokerConnection = brokerConnection;
    }


    @Override
    public DbDepot createDepot(User user, String name, Broker broker, String brokerId, DbDepot.Type type) {
        Preconditions.checkArgument(broker == Broker.OANDA, "Sorry, we only support " + Broker.OANDA + " at the moment");

        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "The dbDepot must have a name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(brokerId), "The dbDepot must have broker id");
        Preconditions.checkArgument(broker != null, "The broker must be specified");

        Preconditions.checkArgument(!depotRepo.findByBrokerId(brokerId).isPresent(), "There is already a Depot connected with broker " + broker + " with the id '" + brokerId + "'");
        BrokerDepot brokerDepot = brokerConnection.getDepot(brokerId);
        Preconditions.checkArgument(brokerDepot != null, "Unable to fetch a depots from " + broker + " with id '" + brokerId + "'");

        DbDepot newDbDepot = new DbDepot(user.getId(), name, broker, brokerDepot.name, brokerId, brokerDepot.marginRate, brokerDepot.currency, brokerDepot.balance, brokerDepot.unrealizedPl, brokerDepot.realizedPl, brokerDepot.marginUsed, brokerDepot.marginAvail, brokerDepot.openTrades, brokerDepot.openOrders, Instant.now(), true, type);
        newDbDepot = depotRepo.save(newDbDepot);
        return newDbDepot;
    }


    @Override
    public void deleteDepot(DbDepot dbDepot) {
        //TODO Synch with broker and check transactions/orders/balance etc before deleting
        LOG.warn("Deleting dbDepot {}", dbDepot);
        depotRepo.delete(dbDepot);
    }

    @Override
    public DbDepot findDepotById(String id) {
        return depotRepo.findOne(id);
    }

    @Override
    public List<DbDepot> findAll() {
        return depotRepo.findAll();
    }

    @Override
    public DbDepot save(DbDepot dbDepot) {
        return depotRepo.save(dbDepot);
    }
}