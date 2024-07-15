package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.validation.ValidPassword;
import cz.demo.usermanagement.validation.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for UserAPI POST
 */
@Data
@Builder
public class SaveUserRequest {

    /**
     * User's username for registration.
     *
     * @see ValidUsername
     */
    @NotBlank(message = "{validation.userName.mandatory}")
    @ValidUsername
    private String userName;

    /**
     * User's password for registration.
     *
     * @see ValidPassword
     */
    @NotBlank(message = "{validation.password.mandatory}")
    @ValidPassword
    private String password;

    private String firstName;

    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
