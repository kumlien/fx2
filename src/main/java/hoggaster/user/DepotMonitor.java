package hoggaster.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DepotMonitor(DepotRepo depotRepo) {
        this.depotRepo = depotRepo;
    }


    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void synchDepots() {
        LOG.info("*********************  Synching depots...  ******************");
        List<Depot> depots = depotRepo.findAll();
        if(depots == null || depots.isEmpty()) {
            LOG.info("No depots found...");
        } else {
            LOG.info("Whoohaa, found {} depots!", depots.size());
        }

    }



}
