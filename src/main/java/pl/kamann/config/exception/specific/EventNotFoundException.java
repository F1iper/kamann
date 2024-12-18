package pl.kamann.config.exception.specific;


import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;

public class EventNotFoundException extends ApiException {
    public EventNotFoundException() {
        super("Event not found.", HttpStatus.NOT_FOUND, EventCodes.EVENT_NOT_FOUND.name());
    }
}