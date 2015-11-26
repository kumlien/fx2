package hoggaster.domain.users.web;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.domain.users.User;
import hoggaster.domain.users.UserService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * Offers api to deal with users.
 * <p/>
 * TODO Add links to depots
 * <p/>
 * Created by svante2 on 2015-10-03.
 * <p/>
 * add some security: http://spring.io/guides/tutorials/bookmarks/
 */

@RestController
@RequestMapping(UserController.ROOT_URL)
public class UserController {

    public static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    public static final String ROOT_URL = "users";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @ApiOperation(value = "create a new users", code = 201)
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public UserResource createUser(@RequestBody @Valid CreateUserRequest request) {
        LOG.info("Got a createUserRequest: {}", request);
        User user = new User(request.username, request.firstName, request.lastName, request.email1, request.password1);
        userService.create(user);
        return new UserResource(user);
    }

    @ApiOperation(value = "Get (links to) all users")
    @RequestMapping(method = RequestMethod.GET)
    public Resources<UserLink> getUsers() {
        return new Resources<>(userService.findAll().stream().map(UserLink::new).collect(Collectors.toList()));
    }

    @ApiOperation(value = "Get the users with the specified username")
    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public UserResource getUserByUsername(@PathVariable String username) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "The provided username is null or empty");
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        return new UserResource(user);
    }


    class UserResource extends ResourceSupport {
        public final User user;

        UserResource(User user) {
            this.user = user;
            this.add(linkTo(UserController.class, user.username).slash(user.username).withSelfRel());
        }
    }

    class UserLink extends ResourceSupport {
        UserLink(User user) {
            this.add(linkTo(UserController.class, user.username).slash(user.username).withRel("users"));
        }
    }
}
