package pl.kamann.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.event.model.Event;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.user.model.AppUser;

import java.util.List;

public interface UserEventHistoryRepository extends JpaRepository<ClientEventHistory, Long> {
    List<ClientEventHistory> findByUser(AppUser user);

    List<ClientEventHistory> findByEvent(Event event);

    List<ClientEventHistory> findByUserId(Long userId);
}