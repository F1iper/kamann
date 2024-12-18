package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;

public class CannotRegisterForPastEventException extends ApiException {
    public CannotRegisterForPastEventException() {
        super("Cannot register for past events.", HttpStatus.BAD_REQUEST, EventCodes.PAST_EVENT_ERROR.name());
    }
}