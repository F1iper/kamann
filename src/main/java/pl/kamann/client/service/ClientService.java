package pl.kamann.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.history.repository.UserCardHistoryRepository;
import pl.kamann.history.repository.UserEventHistoryRepository;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final UserEventHistoryRepository userEventHistoryRepository;
    private final UserCardHistoryRepository userCardHistoryRepository;
    private final EventMapper eventMapper;
    private final EntityLookupService lookupService;

    public List<EventDto> getUpcomingEvents(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        List<Event> events = eventRepository.findUpcomingEventsForUser(user, LocalDateTime.now());
        return events.stream()
                .map(event -> eventMapper.toDto(event, List.of()))
                .collect(Collectors.toList());
    }

    public Page<EventDto> searchEvents(LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        return eventRepository.findFilteredEvents(startDate, endDate, null, null, keyword, pageable)
                .map(event -> eventMapper.toDto(event, List.of()));
    }

    public Attendance joinEvent(Long eventId, Long userId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new ApiException("Event is fully booked.", HttpStatus.BAD_REQUEST, Codes.EVENT_FULL);
        }

        return attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> attendanceRepository.save(
                        new Attendance(null, user, event, AttendanceStatus.PRESENT, LocalDateTime.now())
                ));
    }

    public Attendance cancelAttendance(Long eventId, Long userId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException("Attendance not found.", HttpStatus.NOT_FOUND, Codes.ATTENDANCE_NOT_FOUND));

        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.", HttpStatus.BAD_REQUEST, Codes.INVALID_ATTENDANCE_STATE);
        }

        boolean lateCancel = LocalDateTime.now().isAfter(event.getStartTime().minusHours(6));
        if (lateCancel) {
            deductMembershipEntry(user.getId());
            attendance.setStatus(AttendanceStatus.LATE_CANCEL);
        } else {
            attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        }

        attendance.setTimestamp(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<ClientEventHistory> getOwnEventHistory(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        return userEventHistoryRepository.findByUser(user);
    }

    public List<ClientMembershipCardHistory> getOwnCardHistory(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        return userCardHistoryRepository.findByUser(user);
    }

    public MembershipCard purchaseMembershipCard(Long userId, MembershipCardType type) {
        AppUser user = lookupService.findUserById(userId);

        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(type);
        card.setEntrancesLeft(type.getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(type.getValidDays()));
        card.setPurchaseDate(LocalDateTime.now());
        card.setPaid(false);

        return membershipCardRepository.save(card);
    }

    public void approvePayment(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException("Membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND));
        card.setPaid(true);
        membershipCardRepository.save(card);
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
