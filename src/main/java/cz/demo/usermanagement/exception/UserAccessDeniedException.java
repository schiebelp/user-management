package cz.demo.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserAccessDeniedException extends org.springframework.security.access.AccessDeniedException {

    public UserAccessDeniedException(String message){
        super(message);
    }

}
