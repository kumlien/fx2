package hoggaster.user.web;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import hoggaster.user.User;
import hoggaster.user.UserService;
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
 *
 * TODO Add links to depots
 *
 * Created by svante2 on 2015-10-03.
 *
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


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public UserResource createUser(@RequestBody @Valid CreateUserRequest request) {
        LOG.info("Got a createUserRequest: {}", request);
        User user = new User(request.username, request.firstName, request.lastName, request.email1, request.password1);
        userService.create(user);
        return new UserResource(user);
    }

    @RequestMapping
    public Resources<UserResource> getUsers() {
        return new Resources<>(userService.findAll().stream().map(UserResource::new).collect(Collectors.toList()));
    }

    @RequestMapping("/{id}")
    public UserResource getUserByUsername(@PathVariable String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "The provided username is null or empty");
        User user = userService.getUserByUsername(id).orElseThrow(() -> new UserNotFoundException(id));
        return new UserResource(user);
    }


    class UserResource extends ResourceSupport {

        private final User user;

        UserResource(User user) {
            this.user = user;
            this.add(linkTo(UserController.class, user.username).slash(user.username).withSelfRel());
        }

        public User getUser() {
            return user;
        }
    }




}
