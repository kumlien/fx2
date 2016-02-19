package hoggaster.web.security;

import hoggaster.domain.users.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by svante.kumlien on 19.02.16.
 */
public class MongoUserDetailsService extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MongoUserDetailsService.class);

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    public MongoUserDetailsService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        LOG.info("Checking password for user {}", userDetails.getUsername());
        if (!passwordEncoder.matches(authentication.getCredentials().toString(), userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        final Optional<hoggaster.domain.users.User> dbUser = userRepo.findByUsername(username);
        if(!dbUser.isPresent()) {
            throw new UsernameNotFoundException(username);
        }
        return new User(username, dbUser.get().getPassword(), dbUser.get().getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
    }
}
