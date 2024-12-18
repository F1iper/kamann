package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;

public class EventTypeNotFoundException extends ApiException {
    public EventTypeNotFoundException() {
        super("Event type not found.", HttpStatus.NOT_FOUND, EventCodes.EVENT_TYPE_NOT_FOUND.name());
    }
}