package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.InstructorCodes;
import pl.kamann.config.exception.handler.ApiException;

public class InstructorBusyException extends ApiException {
    public InstructorBusyException() {
        super("Instructor is busy during the requested time slot.", HttpStatus.CONFLICT, InstructorCodes.INSTRUCTOR_BUSY.name());
    }
}