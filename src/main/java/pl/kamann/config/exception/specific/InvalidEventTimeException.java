package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class InvalidEventTimeException extends ApiException {
    public InvalidEventTimeException() {
        super("Invalid time specified for the event.", HttpStatus.BAD_REQUEST, Codes.INVALID_EVENT_TIME);
    }
}