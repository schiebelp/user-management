package cz.demo.usermanagement.controller;


import cz.demo.usermanagement.config.PasswordEncoder;
import cz.demo.usermanagement.controller.api.UserApi;
import cz.demo.usermanagement.controller.dto.GetUserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.exception.InvalidUserException;
import cz.demo.usermanagement.exception.UserServerException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.service.UserService;
import cz.demo.usermanagement.service.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST API controller
 */
@RestController
@Validated //required for @Valid on method parameters such as @RequestParam, @PathVariable, @RequestHeader
@RequestMapping(value = "api/users", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /users : Create a new user
     */
    @Override
    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody SaveUserRequest saveUserRequest) {
        log.info("Started create user" + saveUserRequest);

        if (saveUserRequest.getPassword() == null || saveUserRequest.getPassword().isEmpty()) {
            throw new InvalidUserException("Password is required");
        }

        encodePassword(saveUserRequest);

        userService.createUser(userMapper.toUser(null, saveUserRequest));

        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /users/{id} : Delete a user by ID
     *
    */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "id") Integer id) {
        log.info("Started delete user with id = " + id);

        userService.deleteUser(id);

        return ResponseEntity.ok().build();
    }

    /**
     * GET /users : Get all users
     */
    @GetMapping
    public ResponseEntity<List<GetUserResponse>> getAllUsers() {
        log.info("Started get all users");

        List<User> users = userService.getAllUsers();

        return ResponseEntity.ok(users.stream()
                .map(userMapper::userToGetUserResponseData).toList());
    }

    /**
     * GET /users/{id} : Get a user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GetUserResponse> getUserById(
            @PathVariable("id")
            Integer id) {
        log.info("Started get user with id = " + id);

        User user = userService.getUserById(id);

        return ResponseEntity.ok(userMapper.userToGetUserResponseData(user));
    }

    /**
     * PUT /users/{id} : Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable(value = "id") Long id,
            @Valid @RequestBody SaveUserRequest saveUserRequest,
            Principal principal) {
        log.info("Started update user with id = " + id);

        if (principal == null) {
            log.info("Principal not available");
            throw new UserServerException("Principal not available"); // should never happen
        }

        String loggedUserName = principal.getName();
        log.info("Logged user name = " + loggedUserName);
        if (loggedUserName == null) {
            throw new UserServerException("Missing logged user name"); // should never happen
        }

        if (saveUserRequest.getPassword() == null || saveUserRequest.getPassword().isEmpty()) {
            throw new InvalidUserException("Password is required");
        }

        encodePassword(saveUserRequest);

        userService.updateUser(userMapper.toUser(id, saveUserRequest), loggedUserName);

        return ResponseEntity.ok().build();
    }

    private void encodePassword(SaveUserRequest saveUserRequest) {
        String encodedPassword = passwordEncoder
                .encode(saveUserRequest.getPassword());

        saveUserRequest.setPassword(encodedPassword);
    }

}
