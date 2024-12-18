package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;

public class InvalidEventTimeException extends ApiException {
    public InvalidEventTimeException() {
        super("Invalid time specified for the event.", HttpStatus.BAD_REQUEST, EventCodes.INVALID_EVENT_TIME.name());
    }
}