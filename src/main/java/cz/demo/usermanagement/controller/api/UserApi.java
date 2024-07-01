package cz.demo.usermanagement.controller.api;

import cz.demo.usermanagement.controller.dto.GetUserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
            description = "Creates new or updates existing person. Returns created/updated person with id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    ResponseEntity<Void> createUser(SaveUserRequest saveUserRequest);

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
            description = "Deletes user, will silently fail on non existent user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
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
    ResponseEntity<List<GetUserResponse>> getAllUsers();

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
    ResponseEntity<GetUserResponse> getUserById(@PathVariable("id") Integer id);
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
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "500 Internal Server Error") })
    @PutMapping("/{id}")
    ResponseEntity<Void> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody SaveUserRequest saveUserRequest,
            Principal principal);

}
