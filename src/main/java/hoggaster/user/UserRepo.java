package hoggaster.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Created by svante2 on 2015-10-03.
 */
public interface UserRepo extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
}
