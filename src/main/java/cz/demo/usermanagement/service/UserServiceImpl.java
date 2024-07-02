package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UnauthorizedException;
import cz.demo.usermanagement.exception.UserAlreadyExistsException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.UserRepository;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business layer for User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${spring.admin.username}")
    private String adminUsername;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserServiceSupport userServiceSupport;

    @Override
    public User createUser(User request) {
        log.info("Started create user" + request);

        Optional<UserEntity> optionalUsers= userRepository.findByUserName(request.getUserName());
        if(optionalUsers.isPresent()){
            throw new UserAlreadyExistsException("User already registered with given userName "+request.getUserName());
        }

        userServiceSupport.encodePassword(request);

        UserEntity userEntity = userRepository.save(userMapper.toUserEntity(request));

        log.info("Succesfuly created user with id = " + userEntity.getId());

        return userMapper.toUser(userEntity);

    }

    @Override
    public User updateUser(User request, String loggedUserName) {
        Integer id = request.getId();

        log.info("Started update user with id = " + id);

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with given id " + id));

        // Only admin or same user can update
        if(!existingUser.getUserName().equals(loggedUserName) && !loggedUserName.equals(adminUsername)) {

            log.info("Only admin {} or same user {} can update", adminUsername, existingUser.getUserName());
            throw new UnauthorizedException("Only admin or same user can update");

        }

        // A way to preserve existing data - ignore null/empty values
        if (!userServiceSupport.updateIfNotNullAndChanged(request, existingUser)){
            log.info("Nothing to change at user with id = " + id);
            return userMapper.toUser(existingUser);
        }

        UserEntity saved = userRepository.save(existingUser);

        log.info("Succesfuly updated user {} ", saved);

        return userMapper.toUser(saved);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Started get all users");
        return userRepository.findAll().stream()
                .map(userMapper::toUser)
                .toList();
    }

    @Override
    public User getUserById(Integer userId) {
        log.info("Started get user with id = " + userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        log.info("Found user = " + user);

        return userMapper.toUser(user);
    }

    @Override
    public void deleteUser(Integer id) {
        log.info("Started delete user with id = " + id);

        userRepository.findById(id)
                .ifPresentOrElse(
                        user -> userRepository.deleteById(id), // action if value is present
                        () -> { throw new UserNotFoundException(String.valueOf(id)); } // action if value is empty
                );

        log.info("Succesfuly deleted user with id = " + id);
    }



}
