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
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.systemevents.EventHistoryLogEvent;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final EntityLookupService lookupService;
    private final ClientMembershipCardService clientMembershipCardService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Attendance joinEvent(Long occurrenceEventId) {
        var client = lookupService.getLoggedInUser();
        var occurrenceEvent = lookupService.findOccurrenceEventByOccurrenceEventId(occurrenceEventId);

        // Ensure the user isn't already registered for the same occurrence
        if (attendanceRepository.findByUserAndOccurrenceEvent(client, occurrenceEvent).isPresent()) {
            throw new ApiException(
                    "Client is already registered for the event.",
                    HttpStatus.CONFLICT,
                    AttendanceCodes.ALREADY_REGISTERED.name()
            );
        }

        // Deduct an entry from the client's membership card
        clientMembershipCardService.deductEntry(client.getId());

        // Create a new attendance record
        var attendance = new Attendance();
        attendance.setUser(client);
        attendance.setOccurrenceEvent(occurrenceEvent);  // Set the occurrence event
        attendance.setStatus(AttendanceStatus.REGISTERED);
        attendanceRepository.save(attendance);

        // Publish an event history log
        eventPublisher.publishEvent(new EventHistoryLogEvent(client, occurrenceEvent, AttendanceStatus.REGISTERED));

        return attendance;
    }

    @Transactional
    public Attendance cancelAttendance(Long occurrenceEventId) {
        var clientId = lookupService.getLoggedInUser().getId();
        var attendance = attendanceRepository.findByUserAndOccurrenceEvent(
                        lookupService.findUserById(clientId), lookupService.findOccurrenceEventByOccurrenceEventId(occurrenceEventId))
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

        var occurrenceEvent = attendance.getOccurrenceEvent();
        var currentDateTime = LocalDateTime.now();

        LocalDateTime eventStartTime = LocalDateTime.of(occurrenceEvent.getEvent().getStartDate(), occurrenceEvent.getStartTime());

        // Determine whether the cancellation is early or late
        var cancellationType = currentDateTime.isBefore(eventStartTime.minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;

        attendance.setStatus(cancellationType);
        attendanceRepository.save(attendance);

        // Publish event history log for cancellation
        eventPublisher.publishEvent(new EventHistoryLogEvent(attendance.getUser(), occurrenceEvent, cancellationType));

        return attendance;
    }

    public Map<String, Object> getAttendanceSummary() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
