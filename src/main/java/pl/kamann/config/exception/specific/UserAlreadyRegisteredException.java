package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;

public class UserAlreadyRegisteredException extends ApiException {
    public UserAlreadyRegisteredException() {
        super("User is already registered for this event.", HttpStatus.CONFLICT, AttendanceCodes.ALREADY_REGISTERED.name());
    }
}