package pl.kamann.attendance.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.model.Event;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;

    @Transactional
    public void cancelAttendanceByClient(Long eventId, Long userId) {
        cancelAttendance(eventId, userId, false);
    }

    @Transactional
    public void cancelAttendanceByInstructor(Long eventId, Long clientId) {
        cancelAttendance(eventId, clientId, true);
    }

    @Transactional
    public Attendance cancelAttendance(Long eventId, Long userId, boolean isInstructorAction) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException("Attendance not found.", HttpStatus.NOT_FOUND, "ATTENDANCE_NOT_FOUND"));

        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.", HttpStatus.BAD_REQUEST, "INVALID_ATTENDANCE_STATE");
        }

        boolean lateCancel = LocalDateTime.now().isAfter(event.getStartTime().minusHours(6));
        if (lateCancel) {
            deductMembershipEntry(user.getId());
            attendance.setStatus(AttendanceStatus.LATE_CANCEL);
        } else {
            attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        }

        if (isInstructorAction) {
            attendance.setCancelledByInstructor(true);
        }

        attendance.setTimestamp(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void joinEvent(Long eventId, Long userId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new ApiException("Event is fully booked.", HttpStatus.BAD_REQUEST, Codes.EVENT_FULL);
        }

        attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> attendanceRepository.save(
                        new Attendance(null, user, event, AttendanceStatus.REGISTERED, LocalDateTime.now())
                ));
    }

    @Transactional
    public void markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException("Attendance not found.", HttpStatus.NOT_FOUND, Codes.ATTENDANCE_NOT_FOUND));
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void markMembershipAsPaid(Long userId) {
        MembershipCard card = membershipCardRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND));
        card.setPaid(true);
        membershipCardRepository.save(card);
    }

    @Transactional
    private void deductMembershipEntry(Long userId) {
        MembershipCard card = membershipCardRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND));

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException("No available entries left on membership card.", HttpStatus.BAD_REQUEST, Codes.NO_ENTRANCES_LEFT);
        }

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }

    public Map<String, Object> getPersonalAttendanceSummary() {
        AppUser user = lookupService.getLoggedInUser();

        // Retrieve the attendance records for the logged-in user
        List<Attendance> attendances = attendanceRepository.findByUser(user);

        // Group attendance records by status
        long presentCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long lateCancelCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE_CANCEL)
                .count();
        long earlyCancelCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.EARLY_CANCEL)
                .count();

        // Build the summary map
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAttendance", attendances.size());
        summary.put("presentCount", presentCount);
        summary.put("lateCancelCount", lateCancelCount);
        summary.put("earlyCancelCount", earlyCancelCount);

        return summary;
    }
}
