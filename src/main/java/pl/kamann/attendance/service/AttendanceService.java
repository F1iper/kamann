package pl.kamann.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.dto.AttendanceDto;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.history.model.UserEventHistory;
import pl.kamann.history.repository.UserEventHistoryRepository;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final UserEventHistoryRepository userEventHistoryRepository;
    private final EntityLookupService lookupService;
    private final EventMapper eventMapper;

    public Attendance markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        MembershipCard card = validateMembershipCard(user);

        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> new Attendance(null, user, event, status, LocalDateTime.now()));

        validateAttendanceStatusTransition(attendance, status);

        attendance.setStatus(status);
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepository.save(attendance);

        updateEventHistory(user, event, card, status);

        return attendance;
    }

    public Attendance cancelAttendance(Long userId, Long eventId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        AttendanceStatus status = determineCancellationStatus(event);

        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> new Attendance(null, user, event, status, LocalDateTime.now()));

        validateAttendanceStatusTransition(attendance, status);

        attendance.setStatus(status);
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepository.save(attendance);

        updateEventHistory(user, event, null, status);

        return attendance;
    }

    public List<Attendance> getEventAttendance(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        return attendanceRepository.findByEvent(event);
    }

    public List<Attendance> getUserAttendance(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        return attendanceRepository.findByUser(user);
    }

    private MembershipCard validateMembershipCard(AppUser user) {
        MembershipCard card = membershipCardRepository.findActiveCardByUser(user)
                .orElseThrow(() -> new ApiException(
                        "No active membership card found for user.",
                        HttpStatus.BAD_REQUEST,
                        Codes.CARD_NOT_FOUND
                ));

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "User has no entrances left on the membership card.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INSUFFICIENT_ENTRANCES
            );
        }

        if (card.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Membership card is expired.",
                    HttpStatus.BAD_REQUEST,
                    Codes.CARD_EXPIRED
            );
        }

        return card;
    }

    private void validateAttendanceStatusTransition(Attendance existingAttendance, AttendanceStatus newStatus) {
        AttendanceStatus currentStatus = existingAttendance.getStatus();
        if (currentStatus == AttendanceStatus.PRESENT && newStatus != AttendanceStatus.PRESENT) {
            throw new IllegalArgumentException("Cannot change PRESENT status to another status.");
        }
    }

    private AttendanceStatus determineCancellationStatus(Event event) {
        return LocalDateTime.now().isBefore(event.getStartTime().minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;
    }

    private void updateEventHistory(AppUser user, Event event, MembershipCard card, AttendanceStatus status) {
        UserEventHistory history = userEventHistoryRepository.findByUserAndEvent(user, event)
                .orElse(new UserEventHistory());

        history.setUser(user);
        history.setEvent(event);
        history.setStatus(status);
        history.setAttendedDate(LocalDateTime.now());

        if (status == AttendanceStatus.PRESENT && card != null) {
            card.setEntrancesLeft(card.getEntrancesLeft() - 1);
            membershipCardRepository.save(card);
            history.setEntrancesUsed(1);
        } else {
            history.setEntrancesUsed(0);
        }

        userEventHistoryRepository.save(history);
    }

    public List<EventDto> getUpcomingEventsForUser(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        List<Event> upcomingEvents = attendanceRepository.findUpcomingEventsForUser(user, LocalDateTime.now());
        return eventMapper.toDtoList(upcomingEvents);
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

    public Map<String, Object> getAttendanceSummary(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        List<Attendance> attendanceList = attendanceRepository.findByUser(user);

        long totalAttended = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long totalCancelled = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.EARLY_CANCEL || a.getStatus() == AttendanceStatus.LATE_CANCEL)
                .count();

        long totalNoShows = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return Map.of(
                "totalAttended", totalAttended,
                "totalCancelled", totalCancelled,
                "totalNoShows", totalNoShows
        );
    }

    public EventDto getEventDetails(Long eventId, Long userId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElse(null);

        return eventMapper.toDto(event, attendance == null ? List.of() : List.of(attendance));
    }

    public List<EventDto> searchEventsByTimeFrame(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        AppUser user = lookupService.findUserById(userId);
        List<Event> events = attendanceRepository.findEventsByUserAndTimeFrame(user, startDate, endDate);
        return eventMapper.toDtoList(events);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public void bulkMarkAttendance(List<AttendanceDto> attendances) {
        List<Attendance> attendanceEntities = attendances.stream()
                .map(dto -> {
                    AppUser user = lookupService.findUserById(dto.getUserId());
                    Event event = lookupService.findEventById(dto.getEventId());
                    Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                            .orElse(new Attendance(null, user, event, dto.getStatus(), LocalDateTime.now()));

                    attendance.setStatus(dto.getStatus());
                    attendance.setTimestamp(LocalDateTime.now());
                    return attendance;
                })
                .collect(Collectors.toList());

        attendanceRepository.saveAll(attendanceEntities);
    }
}
