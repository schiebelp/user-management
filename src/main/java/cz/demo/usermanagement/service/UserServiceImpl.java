package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UserAccessDeniedException;
import cz.demo.usermanagement.exception.UserAlreadyExistsException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.entity.Role;
import cz.demo.usermanagement.repository.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business layer for User operations
 *
 * Note Transactions:
 *  Default transation isolation Read Committed (prevents dirty reads) is the default isolation level in Postgres, SQL Server, and Oracle
 *  Default Propagation.REQUIRED is the default propagation type of Spring
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
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User request) {
        log.info("Started create user {} in opened transaction: {} ", request, TransactionSynchronizationManager.getCurrentTransactionName());

        Optional<User> optionalUsers= userDAO.findByUserName(request.getUserName());
        if(optionalUsers.isPresent()){
            throw new UserAlreadyExistsException("User already registered with given userName "+request.getUserName());
        }

        assignRoleCreateIfNotExist(request);

        encodePassword(request);

        return userDAO.save(request);
    }

    @Override
    @Transactional
    public User updateUser(User request, String loggedUserName) {
        log.info("Started update user {} in opened transaction: {} ", request, TransactionSynchronizationManager.getCurrentTransactionName());

        User current = userDAO.findById(request.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with given id " + request.getId()));

        assignRoleCreateIfNotExist(request);

        checkUserAccessForModification(current.getUserName(), loggedUserName);

        // enables partial update
        userMapper.update(request, current);

        return userDAO.update(current);
    }

    @Override
    @Transactional(readOnly = true) //for performance
    public List<User> getAllUsers() {
        log.info("Started get all users");
        return userDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true) //for performance
    public User getUserById(Integer userId) {
        log.info("Started get user with id = " + userId);

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        log.info("Found user = " + user);

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Integer id, String loggedUserName) {
        log.info("Started delete user id {} in opened transaction: {} ", id, TransactionSynchronizationManager.getCurrentTransactionName());

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
     *
     * @throws UserAccessDeniedException if user is not author or admin
     */
    private void checkUserAccessForModification(String entityUserName, String loggedUserName) throws UserAccessDeniedException {

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

    /**
     * Apart from assigning roles, makes sure to reuse existing ones
     *
     * @param request User
     */
    private void assignRoleCreateIfNotExist(User request) {
        if (CollectionUtils.isEmpty(request.getRoles())) {
            return;
        }

        Set<Role> roles = request.getRoles().stream()
                .map(role -> roleService.createRoleIfNotExists(role.getName()))
                .collect(Collectors.toSet());

        request.setRoles(roles);
    }

}
