package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.entities.Attendance;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

@Service
@RequiredArgsConstructor
public class InstructorAttendanceService {

    private final SharedAttendanceService sharedAttendanceService;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public void cancelAttendanceForClient(Long eventId, Long clientId) {
        Attendance attendance = sharedAttendanceService.getAttendance(eventId, clientId);

        // Ensure the attendance can be cancelled
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.", HttpStatus.BAD_REQUEST, "INVALID_ATTENDANCE_STATE");
        }

        attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        attendance.setCancelledByInstructor(true);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        Attendance attendance = sharedAttendanceService.getAttendance(eventId, userId);

        // Validate status transition logic (if any)
        if (!isValidStatusChange(attendance.getStatus(), status)) {
            throw new ApiException("Invalid attendance status transition.", HttpStatus.BAD_REQUEST, Codes.INVALID_STATUS_CHANGE);
        }

        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    private boolean isValidStatusChange(AttendanceStatus currentStatus, AttendanceStatus newStatus) {
        // todo implement validation logic
        if (currentStatus == AttendanceStatus.EARLY_CANCEL || currentStatus == AttendanceStatus.LATE_CANCEL) {
            return false;
        }
        return true;
    }
}
