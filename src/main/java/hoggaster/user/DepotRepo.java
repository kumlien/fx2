package hoggaster.user;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by svante2 on 15-09-20.
 */
public interface DepotRepo extends MongoRepository<Depot, String> {
}
