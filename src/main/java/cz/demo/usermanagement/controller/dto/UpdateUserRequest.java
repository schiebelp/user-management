package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.controller.UserController;
import lombok.Data;
import lombok.ToString;

import java.security.Principal;

/**
 * DTO for UserAPI {@link UserController#updateUser(Integer, UpdateUserRequest, Principal)}
 */
@Data
public class UpdateUserRequest {

    private String userName;

    private String password;

    private String firstName;

    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
