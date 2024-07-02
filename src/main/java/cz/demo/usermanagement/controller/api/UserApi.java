package cz.demo.usermanagement.controller.api;

import cz.demo.usermanagement.controller.dto.UpdateUserRequest;
import cz.demo.usermanagement.controller.dto.UserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Api documentation stuff here (to de-bloat controller)
 */
@Tag(name = "Users", description = "the User Api")
public interface UserApi {

    /**
     * POST /users : Create a new user
     *
     * @param saveUserRequest User object that needs to be added to the database (required)
     * @return User created successfully (status code 201)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     */
    @Operation(
            summary = "Create new user",
            description = "Creates new or updates existing person. Returns created person with id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody SaveUserRequest saveUserRequest);

    /**
     * DELETE /users/{id} : Delete a user by ID
     *
     * @param id ID of the user to delete (required)
     * @return User deleted successfully (status code 204)
     *         or Forbidden (status code 403)
     *         or 500 Internal Server Error (status code 500)
     */
    @Operation(
            summary = "Delete a user by ID",
            description = "Deletes user, will return 200 if successful. Will return 404 if user not found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "500 Internal Server Error") })
    ResponseEntity<Void> deleteUser(@PathVariable(value = "id") Integer id);

    /**
     * GET /users : Get all users
     *
     * @return A list of users (status code 200)
     *         or Forbidden (status code 403)
     *         or 500 Internal Server Error (status code 500)
     */
    @Operation(
            summary = "Get all users",
            description = "Simply returns all users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A list of users"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "500 Internal Server Error") })
    @GetMapping
    ResponseEntity<List<UserResponse>> getAllUsers();

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
    @Operation(
            summary = "Get a user by ID",
            description = "Returns a single user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "500 Internal Server Error") })
    @GetMapping("/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Integer id);
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
    @Operation(
            summary = "Update an existing user",
            description = "Updates a single user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request with description"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "500 Internal Server Error") })
    @PutMapping("/{id}")
    ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateUserRequest saveUserRequest,
            Principal principal);

}
