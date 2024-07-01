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
import java.util.Objects;
import java.util.Optional;

/**
 * BUS vrstva manipulace s uzivatelem
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${spring.admin.username}")
    private String adminUsername;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User createUser(User request) {
        log.info("Started create user" + request);

        Optional<UserEntity> optionalUsers= userRepository.findByUserName(request.getUserName());
        if(optionalUsers.isPresent()){
            throw new UserAlreadyExistsException("User already registered with given userName "+request.getUserName());
        }

        UserEntity userEntity = userRepository.save(userMapper.toUserEntity(request));

        log.info("Succesfuly created user with id = " + userEntity.getId());

        return userMapper.toUser(userEntity);

    }

    @Override
    public void updateUser(User request, String loggedUserName) {
        Integer id = request.getId();

        log.info("Started update user with id = " + id);

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with given id " + id));

        // Only admin or same user can update
        if(!existingUser.getUserName().equals(loggedUserName) &&
                !loggedUserName.equals(adminUsername)) {
            log.info("Only admin {} or same user {} can update", adminUsername, existingUser.getUserName());
            throw new UnauthorizedException("Only admin or same user can update");
        }

        // Aktualizace hodnot pouze pokud byly vyplněny a jsou jiné než aktuální
        updateIfNotNullAndChanged(request, existingUser);

        userRepository.save(existingUser);

        log.info("Succesfuly updated user with id = " + id);
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

        userRepository.deleteById(id);

        log.info("Succesfuly deleted user with id = " + id);
    }

    private static void updateIfNotNullAndChanged(User request, UserEntity existingUser) {

        if(request.getFirstName() != null && !Objects.equals(request.getFirstName(),existingUser.getFirstName() )) {
            existingUser.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null && !Objects.equals(request.getLastName(), existingUser.getLastName())) {
            existingUser.setLastName(request.getLastName());
        }

        if(request.getUserName() != null && !Objects.equals(request.getUserName(), existingUser.getUserName())) {
            existingUser.setUserName(request.getUserName());
        }

        if(request.getPassword() != null && !Objects.equals(request.getPassword(), existingUser.getPassword())) {
            existingUser.setPassword(request.getPassword());
        }

    }

}
