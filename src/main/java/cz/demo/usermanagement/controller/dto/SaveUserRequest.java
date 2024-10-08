package cz.demo.usermanagement.controller.dto;

import cz.demo.usermanagement.validation.ValidPassword;
import cz.demo.usermanagement.validation.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

/**
 * DTO for UserAPI POST, PUT
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
    private String userName; // todo check ValidSubmissionPayload https://github.com/corona-warn-app/cwa-server/blob/main/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java
                                //app.coronawarn.server.services.submission.validation.ValidSubmissionPayload.SubmissionPayloadValidator#isValid
                                //app.coronawarn.server.common.persistence.domain.validation.ValidCountry
    /**
     * User's password for registration.
     *
     * @see ValidPassword
     */
    @NotBlank(message = "{validation.password.mandatory}")
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
