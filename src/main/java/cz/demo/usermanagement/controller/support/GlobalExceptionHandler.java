package cz.demo.usermanagement.controller.support;

import cz.demo.usermanagement.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({
            InvalidUserException.class,
            UnauthorizedException.class,
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            UserServerException.class
    })
    public ProblemDetail customExceptionHandler(Exception ex, WebRequest request) {
        log.info("Custom Exception: " + ex.getMessage());
        HttpStatus status = getStatusFromException(ex.getClass());

        return ProblemDetail.forStatusAndDetail(status, ex.getMessage());
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