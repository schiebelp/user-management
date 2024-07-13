package cz.demo.usermanagement.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring security configuration:
 **
 * Note: Since we use Basic access authentication, we assume the api is deployed to already secured cluster of services.
 *
 * Fully Secure API in my mind:
 * - HTTPS encryption
 * - JWT tokens for authentication (or OAuth2 etc.)
 * - XSS protection: In case of web client to not render javascript/html back using CPS policy headers
 *      - I believe DB is protected in DAO using Criteria API "prepared statements" (see <a href="https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html">OWASP lilnk</a>).
 * - CSRF protection on/off based on see: <a href="https://www.baeldung.com/csrf-stateless-rest-api">Baeldung link</a>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Enables Basic auth for any endpoints except documentation at /swagger-ui/index.html
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http

                // Disable CORS ("aka exceptions to SOP") so now only in our domain / origin api calls are allowed
                .cors(AbstractHttpConfigurer::disable)

                // Disable CSRF - if Basic Auth is sufficient, then probably we run api in already secured cluster of services.
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Non authorized Docs endpoints - http://localhost:8080/swagger-ui/index.html
                        .requestMatchers(
                                "/swagger-ui/**",  // Allow Swagger UI
                                "/v3/api-docs/**",          // Allow Swagger API docs
                                "/swagger-resources/**"     // Allow Swagger resources
                        ).permitAll()
                        // Authenticate all other requests
                        .anyRequest().authenticated()

                )
                // Use basic authentication (user/pass)
                .httpBasic(withDefaults())
                .exceptionHandling(auth -> auth
                        // handle 401 unauthorized requests - missinc credentials
                        .authenticationEntryPoint(customHttp401UnauthorizedEntryPointEntryPoint())
                )
                .build();
    }

    /**
     * Manages Users from DB to authenticate
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, CustomUserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CustomHttp401UnauthorizedEntryPoint customHttp401UnauthorizedEntryPointEntryPoint() {
        return new CustomHttp401UnauthorizedEntryPoint();
    }
}