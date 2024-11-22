package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;

public class InvalidAttendanceStateException extends ApiException {
    public InvalidAttendanceStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_ATTENDANCE_STATE");
    }
}