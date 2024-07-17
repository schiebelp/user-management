package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.validation.ValidPassword;
import cz.demo.usermanagement.validation.ValidUsername;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

/**
 * DTO for UserAPI PATCH
 */
@Data
public class PartialyUpdateUserRequest {

    /**
     * User's username for update.
     *
     * @see ValidUsername
     */
    @ValidUsername
    private String userName;

    /**
     * User's password for update.
     *
     * @see ValidPassword
     */
    @ValidPassword
    private String password;

    @Size(max = 100) // possibly also use pattern to prevent xss
    private String firstName;

    @Size(max = 100) // possibly also use pattern to prevent xss
    private String lastName;

    private Set<String> roles;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
