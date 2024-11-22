package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;

public class AttendanceNotFoundException extends ApiException {
    public AttendanceNotFoundException() {
        super("Attendance record not found", HttpStatus.NOT_FOUND, "ATTENDANCE_NOT_FOUND");
    }
}