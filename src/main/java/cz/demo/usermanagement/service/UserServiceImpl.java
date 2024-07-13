package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UserAccessDeniedException;
import cz.demo.usermanagement.exception.UserAlreadyExistsException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business layer for User operations
 *
 * Note:
 * Default transation isolation Read Committed (prevents dirty reads) is the default isolation level in Postgres, SQL Server, and Oracle
 * Default Propagation.REQUIRED is the default propagation type of Spring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${spring.admin.username}")
    private String adminUsername;
    private static final String ACCESS_DENIED = "Access denied";
    private static final String ACCESS_GRANTED = "Access granted";

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User request) {
        log.info("Started create user {} in opened transaction: {} ", request, TransactionSynchronizationManager.isActualTransactionActive());

        Optional<UserEntity> optionalUsers= userDAO.findByUserName(request.getUserName());
        if(optionalUsers.isPresent()){
            throw new UserAlreadyExistsException("User already registered with given userName "+request.getUserName());
        }

        encodePassword(request);

        UserEntity userEntity = userDAO.save(userMapper.toUserEntity(request));

        log.info("Succesfuly created user with id = " + userEntity.getId());

        return userMapper.toUser(userEntity);
    }

    @Override
    @Transactional
    public User updateUser(User request, String loggedUserName) {
        log.info("Transaction open: " + TransactionSynchronizationManager.isActualTransactionActive());

        Integer id = request.getId();

        log.info("Started update user with id = " + id);

        UserEntity existingUser = userDAO.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with given id " + id));

        checkUserAccessForModification(existingUser.getUserName(), loggedUserName);

        userMapper.updateEntity(request, existingUser);

        UserEntity saved = userDAO.update(existingUser);

        log.info("Succesfuly updated user {} ", saved);

        return userMapper.toUser(saved);
    }

    @Override
    @Transactional(readOnly = true) //for performance
    public List<User> getAllUsers() {
        log.info("Started get all users");
        return userDAO.findAll().stream()
                .map(userMapper::toUser)
                .toList();
    }

    @Override
    @Transactional(readOnly = true) //for performance
    public User getUserById(Integer userId) {
        log.info("Started get user with id = " + userId);

        UserEntity user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        log.info("Found user = " + user);

        return userMapper.toUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id, String loggedUserName) {
        log.info("Started delete user with id = " + id);


        userDAO.findById(id)
                .ifPresentOrElse(
                        user -> {
                            checkUserAccessForModification(user.getUserName(), loggedUserName);
                            userDAO.deleteById(id);
                        },
                        () -> { throw new UserNotFoundException(String.valueOf(id)); } // action if value is empty
                );

        log.info("Succesfuly deleted user with id = " + id);
    }

    /**
     * Encode user password using BCrypt
     */
    protected void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
    }

    /**
     * Check if user is author or admin
     *
     * @param entityUserName entity username
     * @param loggedUserName logged username
     */
    private void checkUserAccessForModification(String entityUserName, String loggedUserName) {

        Objects.requireNonNull(entityUserName, "Entity username cannot be null");
        Objects.requireNonNull(loggedUserName, "Logged username cannot be null");

        if (Objects.equals(entityUserName, loggedUserName)) {
            log.info(ACCESS_GRANTED + ": Same user {} can modify {}", loggedUserName, entityUserName);
            return;
        }

        if (Objects.equals(adminUsername, loggedUserName)) {
            log.info(ACCESS_GRANTED + ": Admin {} can modify {}", loggedUserName, entityUserName);
            return;
        }

        log.info(ACCESS_DENIED + ": User {} cannot modify {}. Only admin or same user is allowed", loggedUserName, entityUserName);
        throw new UserAccessDeniedException(ACCESS_DENIED + ": User " + loggedUserName + " cannot modify " + entityUserName + ". Only admin or same user is allowed");

    }

}
