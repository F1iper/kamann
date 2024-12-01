package pl.kamann.services.instructor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientMembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InstructorAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService lookupService;
    private final ClientMembershipCardService clientMembershipCardService;
    private final ClientEventHistoryService clientEventHistoryService;

    @Transactional
    public void cancelClientAttendance(Long eventId, Long clientId) {
        var attendance = attendanceRepository.findByUserAndEvent(
                        lookupService.findUserById(clientId), lookupService.findEventById(eventId))
                .orElseThrow(() -> new ApiException(
                        "Attendance not found.",
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND));

        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException(
                    "Cannot cancel attendance already marked as PRESENT.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ATTENDANCE_STATE);
        }

        var event = attendance.getEvent();
        var user = attendance.getUser();

        var currentDateTime = LocalDateTime.now();
        var cancellationType = currentDateTime.isBefore(event.getStartTime().minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;

        attendance.setStatus(cancellationType);
        attendanceRepository.save(attendance);

        clientEventHistoryService.logEventHistory(user, event, cancellationType);
    }

    @Transactional
    public void markAttendance(Long eventId, Long clientId, AttendanceStatus status) {
        var attendance = attendanceRepository.findByUserAndEvent(
                        lookupService.findUserById(clientId), lookupService.findEventById(eventId))
                .orElseThrow(() -> new ApiException(
                        "Attendance not found.",
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND));


        if (status == AttendanceStatus.PRESENT) {
            clientMembershipCardService.deductEntry(clientId);
        }

        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }
}
