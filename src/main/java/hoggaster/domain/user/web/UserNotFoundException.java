package hoggaster.domain.user.web;

/**
 * Created by svante2 on 2015-10-08.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("No user found with username '" + username + "'");
    }
}
