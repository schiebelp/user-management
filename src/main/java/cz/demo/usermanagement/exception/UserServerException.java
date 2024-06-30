package cz.demo.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Server error")
public class UserServerException extends RuntimeException {
    public UserServerException(String message) {
        super(message);
    }
}
