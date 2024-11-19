package pl.kamann.config.exception.specific;

public class InvalidEventTimeException extends RuntimeException {
    public InvalidEventTimeException(String message) {
        super(message);
    }
}

