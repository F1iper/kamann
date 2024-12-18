package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.InstructorCodes;
import pl.kamann.config.exception.handler.ApiException;

public class InstructorNotFoundException extends ApiException {
    public InstructorNotFoundException() {
        super("Instructor not found.", HttpStatus.NOT_FOUND, InstructorCodes.INSTRUCTOR_NOT_FOUND.name());
    }
}