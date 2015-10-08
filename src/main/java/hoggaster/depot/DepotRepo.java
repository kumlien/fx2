package hoggaster.depot;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Created by svante2 on 15-09-20.
 */
public interface DepotRepo extends MongoRepository<Depot, String> {

    Optional<Depot> findByBrokerId(String brokerId);
}
