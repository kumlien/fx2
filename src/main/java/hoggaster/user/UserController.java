package hoggaster.user;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Offers api to deal with users.
 *
 * Created by svante2 on 2015-10-03.
 */

@RestController
@RequestMapping(UserController.ROOT_URL)
public class UserController {

    public static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserController.class);

    public static final String ROOT_URL = "users";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(method = RequestMethod.POST)
    public User createUser(@Valid CreateUserRequest request) {
        LOG.info("Got a createUserRequest: {}", request);
        return null;
    }

    @RequestMapping
    public List<User> getUsers() {
        return userService.findAll();
    }



}
