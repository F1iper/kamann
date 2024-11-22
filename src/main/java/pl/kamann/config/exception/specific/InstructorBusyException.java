package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class InstructorBusyException extends ApiException {
    public InstructorBusyException() {
        super("Instructor is busy during the requested time slot.", HttpStatus.CONFLICT, Codes.INSTRUCTOR_BUSY);
    }
}