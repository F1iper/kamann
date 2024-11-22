package pl.kamann.config.exception.specific;


import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class EventNotFoundException extends ApiException {
    public EventNotFoundException() {
        super("Event not found.", HttpStatus.NOT_FOUND, Codes.EVENT_NOT_FOUND);
    }
}