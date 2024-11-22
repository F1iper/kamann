package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class CannotRegisterForPastEventException extends ApiException {
    public CannotRegisterForPastEventException() {
        super("Cannot register for past events.", HttpStatus.BAD_REQUEST, Codes.PAST_EVENT_ERROR);
    }
}