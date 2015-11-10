package hoggaster.depot;

import hoggaster.depot.DbDepot.Type;
import hoggaster.domain.brokers.Broker;
import hoggaster.user.User;

import java.util.List;

/**
 * Created by svante on 15-09-22.
 */
public interface DepotService {


    /**
     * Create a dbDepot with the specified parameters. The dbDepot must exist at the broker side. The service
     * will synchronize the local dbDepot with the dbDepot on the Broker side. The service will also persist
     * the dbDepot.
     *
     * @param user The User to whom the DbDepot belongs
     * @param name A logical name of the dbDepot, unique within the User context.
     * @param broker The broker used for trades on this dbDepot.
     * @param brokerId The id of this dbDepot on the broker side.
     * @param  type The type of dbDepot.
     *
     * @return The newly created dbDepot with synchronized values from the Broker
     */
    DbDepot createDepot(User user, String name, Broker broker, String brokerId, Type type);

    void deleteDepot(DbDepot dbDepot);

    /**
     * Get a depot by id
     * @param id
     * @return The DbDepot or null if not found.
     */
    DbDepot findDepotById(String id);

    List<DbDepot> findAll();


    DbDepot save(DbDepot dbDepot);

}
