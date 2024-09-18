package cz.demo.usermanagement.controller;


import cz.demo.usermanagement.controller.api.UserApi;
import cz.demo.usermanagement.controller.dto.PartialyUpdateUserRequest;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.controller.dto.UserResponse;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    /**
     * POST /users : Create a new user
     */
    @Override
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody SaveUserRequest saveUserRequest) {
        log.info("Started create user" + saveUserRequest);

        User saved = userService.createUser(userMapper.toUser(null, saveUserRequest));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(userMapper.toUserResponse(saved));
    }

    /**
     * DELETE /users/{id} : Delete a user by ID
     *
    */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "id") Integer id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Started delete user with id = " + id);

        userService.deleteUser(id, userDetails.getUsername());

        return ResponseEntity.ok().build();
    }

    /**
     * GET /users : Get all users
     */
    @Override
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Started get all users");

        List<User> users = userService.getAllUsers();

        return ResponseEntity.ok(users.stream()
                .map(userMapper::toUserResponse).toList());
    }

    /**
     * GET /users/{id} : Get user by ID
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Integer id) {
        log.info("Started get user with id = " + id);

        User user = userService.getUserById(id);

        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    /**
     * PUT /users/{id} : Update an existing user
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable(value = "id") Integer id,
                                                   @Valid @RequestBody SaveUserRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Started update user with id {}, request {}, userDetails {}", id, request, userDetails);

        User updated = userService.updateUser(
                userMapper.toUser(id, request),
                userDetails.getUsername());

        return ResponseEntity.ok(userMapper.toUserResponse(updated));
    }

    /**
     * PATCH /users/{id} : Partially update an existing user
     */
    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> partiallyUpdateUser(@PathVariable(value = "id") Integer id,
                                                   @Valid @RequestBody PartialyUpdateUserRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Started partially update user with id {}, request {}, userDetails {}", id, request, userDetails);

        User updated = userService.updateUser(userMapper.toUser(id, request),
                userDetails.getUsername());

        return ResponseEntity.ok(userMapper.toUserResponse(updated));
    }

}
