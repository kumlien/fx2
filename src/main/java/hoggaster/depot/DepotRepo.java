package hoggaster.depot;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by svante2 on 15-09-20.
 */
public interface DepotRepo extends MongoRepository<DbDepot, String> {

    DbDepot findBybrokerId(String brokerId);
}
