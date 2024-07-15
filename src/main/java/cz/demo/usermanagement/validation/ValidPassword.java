package cz.demo.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>User's password Constraints:
 *
 * <ul>
 *      <li>Must be between 8 and 72 characters long:
 *           <ul>
 *               <li>Passwords shorter than 8 characters are considered to be weak (NIST SP800-63B).</li>
 *               <li>Bcrypt seems to cut passwords at 72 characters, owasp recommends exactly this limit:
 *               See <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">OWASP Documentation on Password</a>.</li>
 *           </ul>
 *      </li>
 * </ul>
 *
 * Note: <a href="https://www.baeldung.com/spring-mvc-custom-validator">Spring MVC Custom Validator</a>
 */
@Size(min = 8, max = 72, message = "{validation.password.size}")
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidPassword {
    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}