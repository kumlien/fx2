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

    //TODO Define (per depot)
    private static final BigDecimal MIN_MARGIN_AVAILABLE = new BigDecimal(1000);

    @Autowired
    public DepotMonitorImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection broker) {
        this.depotRepo = depotRepo;
        this.broker = broker;
    }


    @Scheduled(fixedRate = ONE_MINUTE, initialDelay = 10000)
    public void syncAndCheckAllDepots() {
        List<Depot> depots = depotRepo.findAll();
        if (depots == null || depots.isEmpty()) {
            LOG.info("No depots found...");
            return;
        }

        LOG.info("Whoohaa, found {} depots, let's sync and check them", depots.size());
        depots.forEach(depot -> checkDepotMargin(depot, true));
    }


    @Override
    public Depot syncDepot(Depot depot) {
        LOG.info("Start syncing depot {}", depot);
        BrokerDepot depotFromBroker = broker.getDepot(depot.getBrokerId());
        if (depotFromBroker == null) {
            LOG.error("Unable to fetch matching depot from broker: {}", depot);
            depot.setLastSyncOk(false);
        } else {
            depot.setLastSyncOk(true);
            depot.setLastSynchronizedWithBroker(Instant.now());
            depot.updateWithValuesFrom(depotFromBroker);
        }
        depotRepo.save(depot);
        return depot;
    }

    @Override
    public void checkDepotMargin(Depot depot, boolean doSync) {
        if (doSync) {
            depot = syncDepot(depot);
        }
        LOG.warn("Implement depot margin check here...");
    }
}
