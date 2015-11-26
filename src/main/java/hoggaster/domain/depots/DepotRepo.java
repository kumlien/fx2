package hoggaster.domain.depots;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Created by svante2 on 15-09-20.
 */
public interface DepotRepo extends MongoRepository<DbDepot, String> {

    Optional<DbDepot> findByBrokerId(String brokerId);
}
