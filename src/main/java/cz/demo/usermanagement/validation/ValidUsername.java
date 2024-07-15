package cz.demo.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>User's username Constraints:
 *
 * <ul>
 *      <li>Must be between 3 and 30 characters long:
 *          <ul>
 *              <li>Ensures usernames are practical and manageable for users.</li>
 *              <li>Note: PostgreSQL performance is not impacted by varying lengths (e.g., VARCHAR(3) to VARCHAR(30)),
 *                  See <a href="https://www.postgresql.org/docs/current/datatype-character.html">PostgreSQL Documentation on Character Types</a></li>
 *          </ul>
 *      <li>Can only contain alphanumeric characters, underscores, or hyphens:
 *       Prevents special characters that could pose security risks, such as XSS attacks or SQL injection.</li>
 * </ul>
 *
 * Note: <a href="https://www.baeldung.com/spring-mvc-custom-validator">Spring MVC Custom Validator</a>
 */
@Size(min = 3, max = 30, message = "{validation.userName.size}")
@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "{validation.userName.regex}")
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidUsername {
    String message() default "Invalid username";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}