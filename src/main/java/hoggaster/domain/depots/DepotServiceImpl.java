package hoggaster.domain.depots;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.depots.events.DepotUpdateFailedEvent;
import hoggaster.domain.depots.events.DepotUpdatedEvent;
import hoggaster.domain.positions.Position;
import hoggaster.domain.trades.Trade;
import hoggaster.domain.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Created by svante on 15-09-22.
 */
@Service
public class DepotServiceImpl implements DepotService {

    private static final Logger LOG = LoggerFactory.getLogger(DepotServiceImpl.class);

    private final DepotRepo depotRepo;

    //Only support one broker connection for now (oanda)
    private final BrokerConnection brokerConnection;

    private final EventBus depotEventBus;

    @Autowired
    public DepotServiceImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, @Qualifier("depotEventBus") EventBus depotEventBus) {
        this.depotRepo = depotRepo;
        this.brokerConnection = brokerConnection;
        this.depotEventBus = depotEventBus;
    }


    @Override
    public DbDepot createDepot(User user, String name, Broker broker, String brokerId, DbDepot.Type type) {
        Preconditions.checkArgument(broker == Broker.OANDA, "Sorry, we only support " + Broker.OANDA + " at the moment");

        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "The dbDepot must have a name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(brokerId), "The dbDepot must have broker id");
        Preconditions.checkArgument(broker != null, "The broker must be specified");

        Preconditions.checkArgument(!depotRepo.findByBrokerId(brokerId).isPresent(), "There is already a Depot connected with broker " + broker + " with the id '" + brokerId + "'");
        BrokerDepot brokerDepot = brokerConnection.getDepot(brokerId);
        Preconditions.checkArgument(brokerDepot != null, "Unable to fetch a positions from " + broker + " with id '" + brokerId + "'");

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

    @Override
    @Timed
    public DbDepot syncDepot(DbDepot dbDepot) {
        LOG.info("Start syncing dbDepot {}", dbDepot);
        BrokerDepot depotFromBroker = brokerConnection.getDepot(dbDepot.getBrokerId());
        if (depotFromBroker == null) {
            LOG.error("Unable to fetch matching dbDepot from broker: {}", dbDepot);
            dbDepot.setLastSyncOk(false);
        } else {
            final Set<Position> positions = brokerConnection.getPositions(dbDepot.brokerId);
            final Set<Trade> openTrades = brokerConnection.getOpenTrades(dbDepot.getId(), dbDepot.brokerId);
            dbDepot.setLastSyncOk(true);
            dbDepot.updateWithValuesFrom(depotFromBroker, positions, openTrades);
        }
        dbDepot.setLastSynchronizedWithBroker(Instant.now());
        return depotRepo.save(dbDepot);
    }

    @Override
    public CompletableFuture syncDepotAsync(DbDepot dbDepot) {
        return  CompletableFuture.supplyAsync(() -> syncDepot(dbDepot))
                .whenComplete((v,t) -> {
                    if(t != null) {
                        depotEventBus.notify("depots" + v.getId(), new DepotUpdateFailedEvent(v, t));
                    } else {
                        depotEventBus.notify("depots" + v.getId(), new DepotUpdatedEvent(v));
                    }
                });
    }

    @Override
    public Collection<DbDepot> findByUserId(String userId) {
        return depotRepo.findByUserId(userId);
    }
}
