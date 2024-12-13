package pl.kamann.services.instructor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientMembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService lookupService;
    private final ClientMembershipCardService clientMembershipCardService;
    private final ClientEventHistoryService clientEventHistoryService;

    @Transactional
    public void cancelClientAttendance(Long eventId, Long clientId) {
        var attendance = findAttendance(eventId, clientId);

        validateCancelableStatus(attendance);

        var event = attendance.getEvent();
        var user = attendance.getUser();

        var cancellationType = determineCancellationType(event.getStartTime());

        attendance.setStatus(cancellationType);
        attendanceRepository.save(attendance);

        clientEventHistoryService.logEventHistory(user, event, cancellationType);
    }

    @Transactional
    public void markAttendance(Long eventId, Long clientId, AttendanceStatus status) {
        var attendance = findAttendance(eventId, clientId);

        validateAttendanceMarking(attendance, status);

        if (status == AttendanceStatus.PRESENT) {
            clientMembershipCardService.deductEntry(clientId);
        }

        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public List<Attendance> listAttendancesForEvent(Long eventId) {
        var loggedInstructor = lookupService.getLoggedInUser();
        return attendanceRepository.findAllByEventAndInstructor(eventId, loggedInstructor.getId());
    }

    public Attendance getAttendanceDetails(Long eventId, Long clientId) {
        return findAttendance(eventId, clientId);
    }

    private Attendance findAttendance(Long eventId, Long clientId) {
        var user = lookupService.findUserById(clientId);
        var event = lookupService.findEventById(eventId);
        return attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException(
                        "Attendance not found for user: " + clientId + " and event: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND));
    }

    private void validateCancelableStatus(Attendance attendance) {
        var currentStatus = attendance.getStatus();
        if (currentStatus == AttendanceStatus.PRESENT) {
            throw new ApiException(
                    "Cannot cancel attendance already marked as PRESENT.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ATTENDANCE_STATE);
        }
        if (currentStatus != AttendanceStatus.REGISTERED && currentStatus != AttendanceStatus.WAITLISTED) {
            throw new ApiException(
                    "Cannot cancel attendance in current state: " + currentStatus,
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ATTENDANCE_STATE);
        }
    }

    private AttendanceStatus determineCancellationType(LocalDateTime eventStartTime) {
        return LocalDateTime.now().isBefore(eventStartTime.minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;
    }

    private void validateAttendanceMarking(Attendance attendance, AttendanceStatus status) {
        var currentStatus = attendance.getStatus();
        if (currentStatus == AttendanceStatus.CANCELED_BY_INSTRUCTOR || currentStatus == AttendanceStatus.CANCELED_BY_CLIENT) {
            throw new ApiException(
                    "Cannot mark attendance for a canceled status. Current status: " + currentStatus,
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ATTENDANCE_STATE);
        }
        if (status == AttendanceStatus.CANCELED_BY_INSTRUCTOR || status == AttendanceStatus.CANCELED_BY_CLIENT) {
            throw new ApiException(
                    "Invalid status update to: " + status + ". Use cancellation methods instead.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ATTENDANCE_STATE);
        }
    }
}
