package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.entities.Event;
import pl.kamann.repositories.UserEventHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientEventHistoryService {

    private final UserEventHistoryRepository userEventHistoryRepository;

    public void updateEventHistory(AppUser user, Event event, AttendanceStatus status) {
        var history = new ClientEventHistory();
        history.setUser(user);
        history.setEvent(event);
        history.setStatus(status);
        history.setAttendedDate(LocalDateTime.now());
        history.setEntrancesUsed(status == AttendanceStatus.PRESENT ? 1 : 0);

        userEventHistoryRepository.save(history);
    }

    public void logEventHistory(AppUser user, Event event, AttendanceStatus status) {
        var history = new ClientEventHistory();
        history.setUser(user);
        history.setEvent(event);
        history.setStatus(status);
        history.setAttendedDate(LocalDateTime.now());

        if (status == AttendanceStatus.PRESENT) {
            history.setEntrancesUsed(1);
        } else {
            history.setEntrancesUsed(0);
        }

        userEventHistoryRepository.save(history);
    }
}