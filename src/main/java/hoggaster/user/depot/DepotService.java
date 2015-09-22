package hoggaster.user.depot;

import hoggaster.domain.Broker;
import hoggaster.user.User;

/**
 * Created by svante on 15-09-22.
 */
public interface DepotService {


    /**
     * Create a depot with the specified parameters. The depot must exist at the broker side. The service
     * will synchronize the local depot with the depot on the Broker side. The service will also persist
     * the depot.
     *
     * @param user The User to whom the Depot belongs
     * @param name A logical name of the depot, unique within the User context.
     * @param broker The broker used for trades on this depot.
     * @param brokerId The id of this depot on the broker side.
     *
     * @return The newly created depot with synchronized values from the Broker
     */
    Depot createDepot(User user, String name, Broker broker, String brokerId);

    void deleteDepot(Depot depot);

    Depot findDepotById(String id);


}
