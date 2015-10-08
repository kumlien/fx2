package hoggaster.akka.depot;

import akka.actor.Props;
import akka.actor.UntypedActor;
import hoggaster.depot.DbDepot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by svante2 on 2015-10-07.
 */
public class DepotActor extends UntypedActor {

    private static final Logger LOG = LoggerFactory.getLogger(DepotActor.class);

    private final DbDepot dbDbDepot;

    public DepotActor(DbDepot dbDbDepot) {
        this.dbDbDepot = dbDbDepot;
    }


    @Override
    public void onReceive(Object o) throws Exception {
        LOG.info("Got a message: {}", o);
    }

    public static Props props(DbDepot dbDepot) {
        return Props.create(() -> new DepotActor(dbDepot));
    }
}
