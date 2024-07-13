package cz.demo.usermanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Custom handler for REST API controller
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * BUSiness logic exc. handling
     */
    @ExceptionHandler({
            UserAccessDeniedException.class,
            UserAlreadyExistsException.class,
            UserNotFoundException.class})
    public ProblemDetail handleCustomExceptions(Exception ex, WebRequest request) {
        log.info("Custom exception: {} of request: {}", ex.getMessage(), request);

        HttpStatus status = getStatusFromException(ex.getClass());

        return ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    }

    /**
     * Authorization exc. handling, default code is 500 even if its the clients fault
     */
    @ExceptionHandler({AuthorizationDeniedException.class})
    public ProblemDetail handleAccessDeniedException(Exception ex, WebRequest request) {
        log.info("Custom exception: {} of request: {}", ex.getMessage(), request);

        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * RequestBody annotated exc. handling
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Body request invalid exception: {} of request: {}", ex.getMessage(), request);

        String errors = getErrorMessages(ex.getBindingResult()).toString();

        return new ResponseEntity<>(ProblemDetail.forStatusAndDetail(status, errors), status);
    }

    /**
     * PathVariable annotated parameters exc. handling
     * */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.info("Path variable mismatch exception: {}", ex.getMessage());
        String name = ex.getName();
        String type = getType(ex);
        Object value = ex.getValue();
        String message = String.format("Invalid '%s' supplied. Should be a valid '%s' and '%s' isn't!",
                name, type, value);

        return ProblemDetail.forStatusAndDetail(BAD_REQUEST, message);
    }

    /**
     * General exc. handling
     */
    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleGeneralException(Throwable ex) {
        log.error("Unexpected error occurred", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    //new

    private List<String> getErrorMessages(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
    }

    private String getType(MethodArgumentTypeMismatchException ex) {
        return Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("Unknown");
    }

    private static HttpStatus getStatusFromException(Class<? extends Exception> exceptionClass) {
        ResponseStatus responseStatus = exceptionClass.getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        // Default to INTERNAL_SERVER_ERROR if no @ResponseStatus found
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}