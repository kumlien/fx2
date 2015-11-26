package hoggaster.domain.users.web;

/**
 * Created by svante2 on 2015-10-08.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("No users found with username '" + username + "'");
    }
}
