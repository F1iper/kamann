package pl.kamann.services.attendance.shared;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.model.Event;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

@Service
@RequiredArgsConstructor
public class SharedAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;

    public Attendance getAttendance(Long eventId, Long userId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        return attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException("Attendance not found.", HttpStatus.NOT_FOUND, Codes.ATTENDANCE_NOT_FOUND));
    }

    @Transactional
    public void deductMembershipEntry(Long userId) {
        MembershipCard card = membershipCardRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND));

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException("No available entries left on membership card.", HttpStatus.BAD_REQUEST, Codes.NO_ENTRANCES_LEFT);
        }

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }
}
