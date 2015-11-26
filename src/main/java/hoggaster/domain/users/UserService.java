package hoggaster.domain.users;

import java.util.List;
import java.util.Optional;

/**
 * Created by svante2 on 2015-10-03.
 */
public interface UserService {

    User create (User user);

    void update (User user);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserById(String id);

    void delete(User user);

    List<User> findAll();
}
