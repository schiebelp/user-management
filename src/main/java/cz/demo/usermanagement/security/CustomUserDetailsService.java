package cz.demo.usermanagement.security;


import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.repository.enums.ROLE;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Concrete security implementation to load user specific data from Users repository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    @Value("${spring.admin.username}")
    private String adminUsername;

    @Value("${spring.admin.password}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.info("Started load user by username = " + username);

        if(username.equals(adminUsername)) {
            log.info( "Found admin user with username = " + username);
            return buildAdmin();
        }

        User existingUser = userDAO.findByUserName(username, true)
                .orElseGet(() -> {
                log.error("UserNotFoundException: User not found with given username " + username);
                throw new UserNotFoundException("User not found with given username " + username);
            });

        log.info("Found user with username = " + username);

        return buildUser(existingUser);
    }

    private static UserDetails buildUser(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private UserDetails buildAdmin() {
        return org.springframework.security.core.userdetails.User.withUsername(adminUsername)
                .password(adminPassword)
                .authorities(ROLE.ROLE_ADMIN.name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Get user roles to spring security GrantedAuthority
     * @param user User
     * @return Collection of GrantedAuthority
     */
    private static Collection<GrantedAuthority> getAuthorities(User user) {
        log.info("Start get authorities for User: " + user.getUserName());
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
    }

}
