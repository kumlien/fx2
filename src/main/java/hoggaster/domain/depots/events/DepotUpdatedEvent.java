package hoggaster.domain.depots.events;

import hoggaster.domain.depots.DbDepot;
import reactor.bus.Event;

/**
 * Created by svante.kumlien on 07.03.16.
 */
public class DepotUpdatedEvent extends Event<DbDepot> {

    public DepotUpdatedEvent(DbDepot depot) {
        super(depot);
    }
}
