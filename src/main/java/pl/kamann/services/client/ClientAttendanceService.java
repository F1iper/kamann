package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService lookupService;
    private final ClientMembershipCardService clientMembershipCardService;

    @Transactional
    public Attendance joinEvent(Long occurrenceEventId) {
        AppUser client = lookupService.getLoggedInUser();
        OccurrenceEvent occurrenceEvent = lookupService.findOccurrenceEventByOccurrenceEventId(occurrenceEventId);

        // Check if the client is already registered.
        if (attendanceRepository.findByUserAndOccurrenceEvent(client, occurrenceEvent).isPresent()) {
            throw new ApiException(
                    "Client is already registered for the event.",
                    HttpStatus.CONFLICT,
                    AttendanceCodes.ALREADY_REGISTERED.name()
            );
        }

        // Deduct an entry from the client's membership card.
        clientMembershipCardService.deductEntry(client.getId());

        // Create a new attendance record.
        Attendance attendance = new Attendance();
        attendance.setUser(client);
        attendance.setOccurrenceEvent(occurrenceEvent);
        attendance.setStatus(AttendanceStatus.REGISTERED);
        occurrenceEvent.getParticipants().add(client);

        attendanceRepository.save(attendance);

        return attendance;
    }

    @Transactional
    public Attendance cancelAttendance(Long occurrenceEventId) {
        AppUser currentUser = lookupService.getLoggedInUser();
        OccurrenceEvent occurrenceEvent = lookupService.findOccurrenceEventByOccurrenceEventId(occurrenceEventId);

        Attendance attendance = attendanceRepository.findByUserAndOccurrenceEvent(currentUser, occurrenceEvent)
                .orElseThrow(() -> new ApiException(
                        "Attendance not found for user and event",
                        HttpStatus.NOT_FOUND,
                        AttendanceCodes.ATTENDANCE_NOT_FOUND.name()
                ));

        validateCancellation(occurrenceEvent);

        AttendanceStatus cancellationStatus = determineCancellationStatus(occurrenceEvent);

        updateAttendanceStatus(attendance, cancellationStatus);

        publishCancellationEvent(attendance, occurrenceEvent, cancellationStatus);

        return attendance;
    }

    public AttendanceStatus determineCancellationStatus(OccurrenceEvent occurrenceEvent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = occurrenceEvent.getStart().minusHours(24);
        return now.isBefore(cancellationDeadline)
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;
    }

    public void validateCancellation(OccurrenceEvent occurrenceEvent) {
        if (occurrenceEvent.getStart().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Cannot cancel an occurrence that has already started",
                    HttpStatus.BAD_REQUEST,
                    AttendanceCodes.INVALID_ATTENDANCE_STATE.name()
            );
        }
    }

    private void updateAttendanceStatus(Attendance attendance, AttendanceStatus status) {
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    private void publishCancellationEvent(Attendance attendance, OccurrenceEvent occurrenceEvent,
                                          AttendanceStatus attendanceStatus) {
        // TODO: Implement event publishing if needed.
    }

    public Map<String, Object> getAttendanceSummary() {
        // TODO: Implement attendance summary logic.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
