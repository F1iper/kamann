package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.InstructorCodes;
import pl.kamann.config.exception.handler.ApiException;

public class RegistrationNotFoundException extends ApiException {
    public RegistrationNotFoundException() {
        super("Registration not found.", HttpStatus.NOT_FOUND, InstructorCodes.REGISTRATION_NOT_FOUND.name());
    }
}