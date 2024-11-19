package pl.kamann.config.exception.specific;

public class EventTypeNotFoundException extends RuntimeException {
    public EventTypeNotFoundException(String message) {
        super(message);
    }
}

