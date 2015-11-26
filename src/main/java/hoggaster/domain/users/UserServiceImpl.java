package hoggaster.domain.users;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by svante2 on 2015-10-03.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    @Autowired
    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public User create(User user) {
        validateFieldsNotNullOrEmpty(user);
        checkArgument(!userRepo.findByUsername(user.username).isPresent(), "There is already a users with username '" + user.username + "'");
        return userRepo.save(user);
    }

    private static void validateFieldsNotNullOrEmpty(User user) {
        checkArgument(user != null, "The provided users is null...");
        checkArgument(!Strings.isNullOrEmpty(user.username), "The users username is null or empty");
        checkArgument(!Strings.isNullOrEmpty(user.email), "The users email is null or empty");
        checkArgument(!Strings.isNullOrEmpty(user.firstName), "User first name is null or empty");
        checkArgument(!Strings.isNullOrEmpty(user.lastName), "User last name is null or empty");
        checkArgument(!Strings.isNullOrEmpty(user.password), "User password is null or empty");
    }

    @Override
    public void update(User user) {
        validateFieldsNotNullOrEmpty(user);
        userRepo.save(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "The provided username is null or empty");
        return userRepo.findByUsername(username);
    }

    @Override
    public Optional<User> getUserById(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "The provided is is null or empty");
        return Optional.of(userRepo.findOne(id));
    }

    @Override
    public void delete(User user) {
        //TODO Delete depots and stuff...
        throw new RuntimeException("not done yet...");
    }

    @Override
    public List<User> findAll() {
        return userRepo.findAll();
    }
}
