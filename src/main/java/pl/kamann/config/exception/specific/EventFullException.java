package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;

public class EventFullException extends ApiException {
    public EventFullException() {
        super("Event is fully booked", HttpStatus.BAD_REQUEST, "EVENT_FULL");
    }
}