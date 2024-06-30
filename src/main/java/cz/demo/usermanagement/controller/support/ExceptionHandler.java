package cz.demo.usermanagement.controller.support;

import cz.demo.usermanagement.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;


@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({InvalidUserException.class,
            UnauthorizedException.class,
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            UserServerException.class})
    public ResponseEntity<Object> customExceptionHandler(Exception ex, WebRequest request) {
        log.info("Custom exception handler - Exception: " + ex.getMessage());
        HttpStatus status = getStatusFromException(ex.getClass());
        ErrorMessage errorMessage = new ErrorMessage(
                status.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorMessage, status);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage globalExceptionHandler(Exception ex, WebRequest request) {
        log.info("Global exception handler - Exception: " + ex.getMessage());
        return new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }

    private HttpStatus getStatusFromException(Class<? extends Exception> exceptionClass) {
        ResponseStatus responseStatus = exceptionClass.getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        // Default to INTERNAL_SERVER_ERROR if no @ResponseStatus found
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}