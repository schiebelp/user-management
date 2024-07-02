package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.controller.UserController;
import lombok.Data;

import java.security.Principal;

/**
 * DTO for UserAPI {@link UserController#createUser(SaveUserRequest)}, {@link UserController#getAllUsers()},
 * {@link UserController#getUserById(Integer)} and {@link UserController#updateUser(Integer, UpdateUserRequest, Principal)}
 */
@Data
public class UserResponse {
    private String id;
    private String userName;
    private String firstName;
    private String lastName;
}
