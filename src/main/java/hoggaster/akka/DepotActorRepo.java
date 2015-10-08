package hoggaster.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import hoggaster.akka.depot.DepotActor;
import hoggaster.depot.DbDepot;
import hoggaster.depot.DepotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by svante2 on 2015-10-07.
 */
@Component
public class DepotActorRepo {

    private static final Logger LOG = LoggerFactory.getLogger(DepotActorRepo.class);

    private final DepotService depotService;

    private final ActorSystem depotActorSystem;

    private final Map<String, ActorRef> depotActors = new ConcurrentHashMap<>();


    @Autowired
    public DepotActorRepo(DepotService depotService) {
        this.depotService = depotService;
        depotActorSystem = ActorSystem.create("DepotActorSystem");
    }

    @PostConstruct
    void initDepots() {
        List<DbDepot> dbDepots = depotService.findAll();
        dbDepots.forEach(dbDepot -> {
            ActorRef actorRef = depotActorSystem.actorOf(DepotActor.props(dbDepot), "Depot " + dbDepot.getId() + " (" + dbDepot.getName()+")");
            depotActors.put(dbDepot.getId(), actorRef);
        });
    }
}
