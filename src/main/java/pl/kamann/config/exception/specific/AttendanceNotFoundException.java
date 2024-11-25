package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class AttendanceNotFoundException extends ApiException {
    public AttendanceNotFoundException() {
        super("Attendance record not found", HttpStatus.NOT_FOUND, Codes.ATTENDANCE_NOT_FOUND);
    }
}