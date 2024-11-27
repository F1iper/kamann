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
import pl.kamann.entities.Event;
import pl.kamann.entities.MembershipCard;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;

    @Transactional
    public Attendance joinEvent(Long eventId) {
        AppUser user = lookupService.getLoggedInUser();
        Event event = lookupService.findEventById(eventId);

        // Validate capacity
        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new ApiException("Event is fully booked.", HttpStatus.BAD_REQUEST, "EVENT_FULL");
        }

        // Join event
        return attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> attendanceRepository.save(
                        new Attendance(null, user, event, AttendanceStatus.REGISTERED, LocalDateTime.now())
                ));
    }

    @Transactional
    public Attendance cancelAttendanceForClient(Long eventId) {
        AppUser user = lookupService.getLoggedInUser();
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, lookupService.findEventById(eventId))
                .orElseThrow(() -> new ApiException("Attendance not found.", HttpStatus.NOT_FOUND, "ATTENDANCE_NOT_FOUND"));

        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.", HttpStatus.BAD_REQUEST, "INVALID_ATTENDANCE_STATE");
        }

        attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        attendance.setTimestamp(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Map<String, Object> getAttendanceSummary() {
        AppUser user = lookupService.getLoggedInUser();
        List<Attendance> attendances = attendanceRepository.findByUser(user);

        long presentCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long lateCancelCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE_CANCEL)
                .count();
        long earlyCancelCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.EARLY_CANCEL)
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAttendance", attendances.size());
        summary.put("presentCount", presentCount);
        summary.put("lateCancelCount", lateCancelCount);
        summary.put("earlyCancelCount", earlyCancelCount);

        return summary;
    }

    private void deductMembershipEntry(Long userId) {
        MembershipCard card = membershipCardRepository.findActiveCardByUserId(userId)
                .orElseThrow(() -> new ApiException("Active membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND));

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException("No entrances left on the membership card.", HttpStatus.BAD_REQUEST, Codes.NO_ENTRANCES_LEFT);
        }

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }
}
