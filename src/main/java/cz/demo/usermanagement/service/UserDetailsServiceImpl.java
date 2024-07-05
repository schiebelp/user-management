package cz.demo.usermanagement.service;



import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.repository.UserRepository;
import cz.demo.usermanagement.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Concrete security implementation to load user specific data from Users repository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Value("${spring.admin.username}")
    private String adminUsername;

    @Value("${spring.admin.password}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.info("Started load user by username = " + username);

        if(username.equals(adminUsername)) {
            log.info( "Returning admin user details");
            return User.withUsername(adminUsername)
                    .password(adminPassword)
                    .authorities("ROLE_ADMIN")
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        log.info("Looking for user with username = " + username);

        UserEntity existingUser = repository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with given username " + username));

        log.info("Found existingUser = " + existingUser);

        return User.withUsername(existingUser.getUserName())
                .password(existingUser.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

}
