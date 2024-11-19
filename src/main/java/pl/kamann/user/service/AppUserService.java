package pl.kamann.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.card.model.Card;
import pl.kamann.card.repository.CardRepository;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final CardRepository cardRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Handle user attendance for an event.
     * Validates card status, entrances left, and marks attendance.
     */
    public void attendEvent(AppUser user, Event event) {
        if (!isEventAvailableForAttendance(event)) {
            throw new IllegalArgumentException("This event is not eligible for attendance.");
        }

        Card card = cardRepository.findActiveCardByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User has no active card."));

        if (card.getEntrancesLeft() <= 0) {
            throw new IllegalArgumentException("User's card has no entrances left.");
        }

        decrementCardEntrances(card);

        recordAttendance(user, event);
    }

    private boolean isEventAvailableForAttendance(Event event) {
        return event.getStatus() == EventStatus.UPCOMING &&
                event.getStartTime().isAfter(LocalDateTime.now());
    }

    private void decrementCardEntrances(Card card) {
        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        if (card.getEntrancesLeft() == 0) {
            card.setActive(false);
        }
        cardRepository.save(card);
    }

    private void recordAttendance(AppUser user, Event event) {
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setEvent(event);
        attendance.setTimestamp(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setNotes(null);

        attendanceRepository.save(attendance);
    }
}