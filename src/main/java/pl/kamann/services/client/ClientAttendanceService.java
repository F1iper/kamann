package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.systemevents.EventHistoryLogEvent;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService lookupService;
    private final ClientMembershipCardService clientMembershipCardService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Attendance joinEvent(Long eventId) {
        var client = lookupService.getLoggedInUser();
        var event = lookupService.findEventById(eventId);

        if (attendanceRepository.findByUserAndEvent(client, event).isPresent()) {
            throw new ApiException(
                    "Client is already registered for the event.",
                    HttpStatus.CONFLICT,
                    AttendanceCodes.ALREADY_REGISTERED.name()
            );
        }

        clientMembershipCardService.deductEntry(client.getId());

        var attendance = new Attendance();
        attendance.setUser(client);
        attendance.setEvent(event);
        attendance.setStatus(AttendanceStatus.REGISTERED);
        attendanceRepository.save(attendance);

        eventPublisher.publishEvent(new EventHistoryLogEvent(client, event, AttendanceStatus.REGISTERED));

        return attendance;
    }

    @Transactional
    public Attendance cancelAttendance(Long eventId) {
        var clientId = lookupService.getLoggedInUser().getId();
        var attendance = attendanceRepository.findByUserAndEvent(
                        lookupService.findUserById(clientId), lookupService.findEventById(eventId))
                .orElseThrow(() -> new ApiException(
                        "Attendance not found.",
                        HttpStatus.NOT_FOUND,
                        AttendanceCodes.ATTENDANCE_NOT_FOUND.name()
                ));

        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException(
                    "Cannot cancel attendance already marked as PRESENT.",
                    HttpStatus.BAD_REQUEST,
                    AttendanceCodes.INVALID_ATTENDANCE_STATE.name()
            );
        }

        var event = attendance.getEvent();
        var currentDateTime = LocalDateTime.now();

        LocalDateTime eventStartTime = LocalDateTime.of(event.getStartDate(), event.getTime());

        var cancellationType = currentDateTime.isBefore(eventStartTime.minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;

        attendance.setStatus(cancellationType);
        attendanceRepository.save(attendance);

        eventPublisher.publishEvent(new EventHistoryLogEvent(attendance.getUser(), event, cancellationType));

        return attendance;
    }

    public Map<String, Object> getAttendanceSummary() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
