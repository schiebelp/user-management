package cz.demo.usermanagement.service;

import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Support to the User Service class to keep Service readable
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceSupport {

    private final BCryptPasswordEncoder passwordEncoder;

    protected boolean updateIfNotNullAndChanged(User request, UserEntity existingUser){

        boolean updated = updateCommonIfNotNullAndChanged(request, existingUser);
        updated |= updatePasswordIfNotNullAndChanged(request, existingUser);

        return updated;
    }

    private static boolean updateCommonIfNotNullAndChanged(User request, UserEntity existingUser) {
        boolean updated = false;

        if(request.getFirstName() != null && !Objects.equals(request.getFirstName(),existingUser.getFirstName() )) {
            existingUser.setFirstName(request.getFirstName());
            updated = true;
        }

        if(request.getLastName() != null && !Objects.equals(request.getLastName(), existingUser.getLastName())) {
            existingUser.setLastName(request.getLastName());
            updated = true;
        }

        if(request.getUserName() != null && !Objects.equals(request.getUserName(), existingUser.getUserName())) {
            existingUser.setUserName(request.getUserName());
            updated = true;
        }

        return updated;
    }

    private boolean updatePasswordIfNotNullAndChanged(User request, UserEntity existingUser){

        if(request.getPassword() != null && !Objects.equals(request.getPassword(), existingUser.getPassword())) {
            encodePassword(request);
            existingUser.setPassword(request.getPassword());
            return true;
        }

        return false;
    }

    protected void encodePassword(User user) {
        String encodedPassword = passwordEncoder
                .encode(user.getPassword());

        user.setPassword(encodedPassword);
    }

}
