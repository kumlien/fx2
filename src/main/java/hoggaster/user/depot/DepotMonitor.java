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
        LOG.info("*********************  Synching depots...  ******************");
        List<Depot> depots = depotRepo.findAll();
        if(depots == null || depots.isEmpty()) {
            LOG.info("No depots found...");
            return;
        }

        LOG.info("Whoohaa, found {} depots!", depots.size());
        depots.forEach(this::synchDepot);
    }

    public void synchDepot(Depot depot) {
        LOG.info("Start synching depot {}", depot);
        BrokerDepot depotFromBroker = broker.getDepot(depot.getBrokerId());
        if(depotFromBroker == null) {
            LOG.error("Unable to fetch matching depot from broker: {}", depot);
            return;
        }
    }
}
