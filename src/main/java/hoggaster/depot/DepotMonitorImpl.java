package hoggaster.depot;

import hoggaster.domain.BrokerConnection;
import hoggaster.domain.BrokerDepot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * This component is responsible for monitoring the depots, in particular the margins.
 * <p>
 * Created by svante on 15-09-20.
 */
@Component
public class DepotMonitorImpl implements DepotMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(DepotMonitorImpl.class);

    static final long ONE_MINUTE = 60L * 1000;

    private final DepotRepo depotRepo;

    private final BrokerConnection broker;

    //TODO Define (per dbDepot)
    private static final BigDecimal MIN_MARGIN_AVAILABLE = new BigDecimal(1000);

    @Autowired
    public DepotMonitorImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection broker) {
        this.depotRepo = depotRepo;
        this.broker = broker;
    }


    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 10000)
    public void syncAndCheckAllDepots() {
        List<DbDepot> dbDepots = depotRepo.findAll();
        if (dbDepots == null || dbDepots.isEmpty()) {
            LOG.info("No dbDepots found...");
            return;
        }

        LOG.info("Whoohaa, found {} dbDepots, let's sync and check them", dbDepots.size());
        dbDepots.forEach(depot -> checkDepotMargin(depot, true));
    }


    @Override
    public DbDepot syncDepot(DbDepot dbDepot) {
        LOG.info("Start syncing dbDepot {}", dbDepot);
        BrokerDepot depotFromBroker = broker.getDepot(dbDepot.getBrokerId());
        if (depotFromBroker == null) {
            LOG.error("Unable to fetch matching dbDepot from broker: {}", dbDepot);
            dbDepot.setLastSyncOk(false);
        } else {
            dbDepot.setLastSyncOk(true);
            dbDepot.setLastSynchronizedWithBroker(Instant.now());
            dbDepot.updateWithValuesFrom(depotFromBroker);
        }
        depotRepo.save(dbDepot);
        return dbDepot;
    }

    @Override
    public void checkDepotMargin(DbDepot dbDepot, boolean doSync) {
        if (doSync) {
            dbDepot = syncDepot(dbDepot);
        }
        LOG.warn("Implement dbDepot margin check here...");
    }
}
