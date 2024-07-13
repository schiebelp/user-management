package cz.demo.usermanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * Handling of HTTP 401 (Unauthorized) due not providing Basic Auth credentials
 *
 * Could not be handled in RestControllerAdvice as security filter chain is handled before reaching controller
 *
 * Thanks to: <a href="https://stackoverflow.com/a/77288115">...</a>
 */
@Component
@Slf4j
public class CustomHttp401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.info("Authentication exception: {}", authException.getMessage());

        // Set HTTP status and content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");

        // Write the ProblemDetail JSON to response body
        response.getWriter().write(objectMapper.writeValueAsString(
                ProblemDetail.forStatusAndDetail(
                        HttpStatusCode.valueOf(HttpServletResponse.SC_UNAUTHORIZED), authException.getMessage())));
    }

}