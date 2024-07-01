package cz.demo.usermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * To decode/encode password in WebSecurityConfig and UserController
 */
@Configuration
public class PasswordEncoder extends BCryptPasswordEncoder {
}
