package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.controller.UserController;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for UserAPI {@link UserController#createUser(SaveUserRequest)}
 */
@Data
public class SaveUserRequest {

    @NotBlank(message = "Username is mandatory")
    private String userName;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
