package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class InstructorNotFoundException extends ApiException {
    public InstructorNotFoundException() {
        super("Instructor not found.", HttpStatus.NOT_FOUND, Codes.INSTRUCTOR_NOT_FOUND);
    }
}