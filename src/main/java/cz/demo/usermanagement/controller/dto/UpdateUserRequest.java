package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.validation.ValidPassword;
import cz.demo.usermanagement.validation.ValidUsername;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for UserAPI PATCH, PUT
 */
@Data
public class UpdateUserRequest {

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

    private String firstName;

    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
