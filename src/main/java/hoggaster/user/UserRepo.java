package hoggaster.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by svante2 on 2015-10-03.
 */
public interface UserRepo extends MongoRepository<User, String> {

    List<User> findByUsername(String username);
}
