package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("User not found.", HttpStatus.NOT_FOUND, AuthCodes.USER_NOT_FOUND.name());
    }
}