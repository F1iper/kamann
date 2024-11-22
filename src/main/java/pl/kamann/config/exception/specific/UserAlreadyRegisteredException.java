package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class UserAlreadyRegisteredException extends ApiException {
    public UserAlreadyRegisteredException() {
        super("User is already registered for this event.", HttpStatus.CONFLICT, Codes.ALREADY_REGISTERED);
    }
}