package cz.demo.usermanagement.controller.dto;

import lombok.Data;

import java.util.Set;

/**
 * DTO for UserAPI read operations
 */
@Data
public class UserResponse {
    private String id;
    private String userName;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    // no password!
}
