package pl.kamann.services.instructor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.Attendance;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.services.SharedAttendanceService;

@Service
@RequiredArgsConstructor
public class InstructorAttendanceService {

    private final SharedAttendanceService sharedAttendanceService;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public void cancelAttendanceForClient(Long eventId, Long clientId) {
        Attendance attendance = sharedAttendanceService.getAttendance(eventId, clientId);

        if (!sharedAttendanceService.isAttendanceValidForCancellation(attendance)) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.",
                    HttpStatus.BAD_REQUEST, Codes.INVALID_ATTENDANCE_STATE);
        }

        attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        Attendance attendance = sharedAttendanceService.getAttendance(eventId, userId);

        // Add validation for valid status transitions
        if (!isValidStatusChange(attendance.getStatus(), status)) {
            throw new ApiException("Invalid attendance status transition.",
                    HttpStatus.BAD_REQUEST, Codes.INVALID_STATUS_CHANGE);
        }

        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    private boolean isValidStatusChange(AttendanceStatus currentStatus, AttendanceStatus newStatus) {
        // todo: Logic for validating status transitions (customize as needed)
        return !(currentStatus == AttendanceStatus.PRESENT && newStatus != AttendanceStatus.PRESENT);
    }
}
