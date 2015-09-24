package hoggaster.user.depot;

import hoggaster.domain.BrokerConnection;
import hoggaster.domain.BrokerDepot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This component is responsible for monitoring the depots, in particular the margins.
 *
 * Created by svante on 15-09-20.
 */
@Component
public class DepotMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(DepotMonitor.class);

    private final DepotRepo depotRepo;

    private final BrokerConnection broker;

    @Autowired
    public DepotMonitor(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection broker) {
        this.depotRepo = depotRepo;
        this.broker = broker;
    }


    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void synchDepots() {
        List<Depot> depots = depotRepo.findAll();
        if(depots == null || depots.isEmpty()) {
            LOG.info("No depots found...");
            return;
        }

        LOG.info("Whoohaa, found {} depots, let's sync them", depots.size());
        depots.forEach(this::syncDepot);
    }

    public void syncDepot(Depot depot) {
        LOG.info("Start syncing depot {}", depot);
        try {
            BrokerDepot depotFromBroker = broker.getDepot(depot.getBrokerId());
            if (depotFromBroker == null) {
                LOG.error("Unable to fetch matching depot from broker: {}", depot);
                depot.setLastSyncOk(false);
                depotRepo.save(depot);
                return;
            }
            if (depot.updateWithValuesFrom(depotFromBroker)) {
                LOG.info("Depot has changed, persist it to db");
                depot.setLastSyncOk(true);
                depotRepo.save(depot);
            }
        } catch (Exception e) {
            LOG.error("Error syncing depot {}", depot, e);
            depot.setLastSyncOk(false);
            depotRepo.save(depot);
        }
    }
}
