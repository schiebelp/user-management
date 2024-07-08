package cz.demo.usermanagement.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for UserAPI POST
 */
@Data
@Builder
public class SaveUserRequest {

    @NotBlank(message = "{validation.userName.mandatory}")
    @Size(min = 6, max = 254, message = "{validation.userName.size}") // 254 case it becomes email address, then RFC 5321 applies, see https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html#email-address-validation
    private String userName;

    @NotBlank(message = "{validation.password.mandatory}")
    @Size(min = 8, max = 72, message = "{validation.password.size}") //OWASP bcrypt has a maximum length input length of 72 bytes, see https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
    private String password;

    private String firstName;

    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }
}
