package hoggaster.domain.depots;

import hoggaster.domain.brokers.BrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This component is responsible for monitoring the depots, in particular the margins.
 * <p>
 * Created by svante on 15-09-20.
 */
@Component
public class DepotMonitorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DepotMonitorImpl.class);

    static final long ONE_MINUTE = 60L * 1000;

    private final DepotRepo depotRepo;

    private final BrokerConnection broker;

    private final DepotService depotService;

    @Autowired
    public DepotMonitorImpl(DepotRepo depotRepo, @Qualifier("OandaBrokerConnection") BrokerConnection broker, DepotService depotService) {
        this.depotRepo = depotRepo;
        this.broker = broker;
        this.depotService = depotService;
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




    public void checkDepotMargin(DbDepot dbDepot, boolean doSync) {
        if (doSync) {
            depotService.syncDepot(dbDepot);
        }
        LOG.warn("Implement dbDepot margin check here...");
    }
}
