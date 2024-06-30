package cz.demo.usermanagement.controller;


import cz.demo.usermanagement.config.WebSecurityConfig;
import cz.demo.usermanagement.controller.dto.GetUserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.exception.InvalidUserException;
import cz.demo.usermanagement.exception.UserServerException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.service.UserService;
import cz.demo.usermanagement.service.domain.User;


import io.swagger.annotations.*;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(value = "/", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    private final WebSecurityConfig webSecurityConfig;

    /**
     * POST /users : Create a new user
     *
     * @param saveUserRequest User object that needs to be added to the database (required)
     * @return User created successfully (status code 201)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     */
    @ApiOperation(value = "Create a new user", nickname = "createUser", notes = "", authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={ "Users"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User created successfully"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden") })
    @PostMapping("/users")
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
     * @param id ID of the user to delete (required)
     * @return User deleted successfully (status code 204)
     *         or Forbidden (status code 403)
     *         or User not found (status code 404)
     *         or 500 Internal Server Error (status code 500)
     */
    @ApiOperation(value = "Delete a user by ID", nickname = "deleteUser", notes = "", authorizations = {

            @Authorization(value = "basicAuth")
    }, tags={ "Users"})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "User deleted successfully"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "500 Internal Server Error") })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@ApiParam(value = "ID of the user to delete",required=true) @PathVariable("id") Long id) {
        log.info("Started delete user with id = " + id);

        userService.deleteUser(id);

        return ResponseEntity.ok().build();
    }

    /**
     * GET /users : Get all users
     *
     * @return A list of users (status code 200)
     *         or Forbidden (status code 403)
     *         or 500 Internal Server Error (status code 500)
     */
    @ApiOperation(value = "Get all users", nickname = "getAllUsers", notes = "", response = GetUserResponse.class, responseContainer = "List", authorizations = {

            @Authorization(value = "basicAuth")
    }, tags={ "Users"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A list of users", response = GetUserResponse.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "500 Internal Server Error") })
    @GetMapping("/users")
    public ResponseEntity<List<GetUserResponse>> getAllUsers() {
        log.info("Started get all users");

        List<User> users = userService.getAllUsers();

        return ResponseEntity.ok(users.stream()
                .map(userMapper::userToGetUserResponseData).collect(Collectors.toList()));
    }

    /**
     * GET /users/{id} : Get a user by ID
     * Returns a single user
     *
     * @param id ID of the user to retrieve (required)
     * @return successful operation (status code 200)
     *         or Invalid ID supplied (status code 400)
     *         or Forbidden (status code 403)
     *         or User not found (status code 404)
     *         or 500 Internal Server Error (status code 500)
     */
    @ApiOperation(value = "Get a user by ID", nickname = "getUserById", notes = "Returns a single user", response = GetUserResponse.class, authorizations = {

            @Authorization(value = "basicAuth")
    }, tags={ "Users"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = GetUserResponse.class),
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "500 Internal Server Error") })
    @GetMapping("/users/{id}")
    public ResponseEntity<GetUserResponse> getUserById(
            @ApiParam(value = "ID of the user to retrieve",required=true) @PathVariable("id") Long id) {
        // todo long validace
        log.info("Started get user with id = " + id);

        User user = userService.getUserById(id);

        return ResponseEntity.ok(userMapper.userToGetUserResponseData(user));
    }

    /**
     * PUT /users/{id} : Update an existing user
     *
     * @param id ID of the user to update (required)
     * @param saveUserRequest Updated user object (required)
     * @return User updated successfully (status code 200)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     *         or User not found (status code 404)
     *         or 500 Internal Server Error (status code 500)
     */
    @ApiOperation(value = "Update an existing user", nickname = "updateUser", notes = "", authorizations = {

            @Authorization(value = "basicAuth")
    }, tags={ "Users", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User updated successfully"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "500 Internal Server Error") })
    @PutMapping("/users/{id}")
    public ResponseEntity<Void> updateUser(
            @ApiParam(value = "ID of the user to retrieve",required=true) @PathVariable("id") Long id,
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

        if (saveUserRequest.getPassword() != null || !saveUserRequest.getPassword().isEmpty()) {
            log.info("Encoding password...");

            String password = saveUserRequest.getPassword();

            String encodedPassword = webSecurityConfig.passwordEncoder()
                    .encode(password);

            saveUserRequest.setPassword(encodedPassword);

            log.info("Password encoded, size = " + encodedPassword.length());
        }

    }

}
