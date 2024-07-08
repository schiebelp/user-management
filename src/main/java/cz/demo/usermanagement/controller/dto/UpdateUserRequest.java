package cz.demo.usermanagement.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for UserAPI PATCH, PUT
 */
@Data
public class UpdateUserRequest {

    @Size(min = 6, max = 254, message = "{validation.userName.size}")
    private String userName;

    @Size(min = 6, max = 254, message = "{validation.password.size}")
    private String password;

    private String firstName;

    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
