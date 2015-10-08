package hoggaster.user;

import java.util.List;

/**
 * Created by svante2 on 2015-10-03.
 */
public interface UserService {

    User create (User user);

    void update (User user);

    User getUserByUsername(String username);

    User getUserById(String id);

    void delete(User user);

    List<User> findAll();
}
