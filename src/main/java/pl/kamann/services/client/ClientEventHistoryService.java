package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.UserEventHistoryRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientEventHistoryService {

    private final UserEventHistoryRepository userEventHistoryRepository;
    private final EntityLookupService lookupService;


    public void logEventHistory(AppUser user, Event event, AttendanceStatus status) {
        var history = new ClientEventHistory();
        history.setUser(user);
        history.setEvent(event);
        history.setStatus(status);
        history.setAttendedDate(LocalDateTime.now());
        history.setEntrancesUsed(status == AttendanceStatus.PRESENT ? 1 : 0);
        userEventHistoryRepository.save(history);
    }

    public void updateEventHistory(AppUser user, Long eventId, AttendanceStatus status) {
        Event event = lookupService.findEventById(eventId);
        var history = userEventHistoryRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> createNewHistory(user, event));
        history.setStatus(status);
        history.setAttendedDate(LocalDateTime.now());
        history.setEntrancesUsed(status == AttendanceStatus.PRESENT ? 1 : 0);
        userEventHistoryRepository.save(history);
    }

    private ClientEventHistory createNewHistory(AppUser user, Event event) {
        var history = new ClientEventHistory();
        history.setUser(user);
        history.setEvent(event);
        return history;
    }
}
