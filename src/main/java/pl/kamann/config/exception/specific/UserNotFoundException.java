package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("User not found.", HttpStatus.NOT_FOUND, Codes.USER_NOT_FOUND);
    }
}