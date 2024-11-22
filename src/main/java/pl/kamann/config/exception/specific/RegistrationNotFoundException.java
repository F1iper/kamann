package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class RegistrationNotFoundException extends ApiException {
    public RegistrationNotFoundException() {
        super("Registration not found.", HttpStatus.NOT_FOUND, Codes.REGISTRATION_NOT_FOUND);
    }
}