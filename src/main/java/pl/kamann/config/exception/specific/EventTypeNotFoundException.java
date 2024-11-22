package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class EventTypeNotFoundException extends ApiException {
    public EventTypeNotFoundException() {
        super("Event type not found.", HttpStatus.NOT_FOUND, Codes.EVENT_TYPE_NOT_FOUND);
    }
}