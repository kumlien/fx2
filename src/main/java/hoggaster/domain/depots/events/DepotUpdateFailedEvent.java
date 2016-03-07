package hoggaster.domain.depots.events;

import hoggaster.domain.depots.DbDepot;
import reactor.bus.Event;

/**
 * Created by svante.kumlien on 07.03.16.
 */
public class DepotUpdateFailedEvent extends Event<DbDepot> {

    public final Throwable cause;

    public DepotUpdateFailedEvent(DbDepot depot, Throwable throwable) {

        super(depot);
        this.cause = throwable;
    }
}
