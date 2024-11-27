package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.Event;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.entities.AppUser;

import java.util.List;

public interface UserEventHistoryRepository extends JpaRepository<ClientEventHistory, Long> {
    List<ClientEventHistory> findByUser(AppUser user);

    List<ClientEventHistory> findByEvent(Event event);

    List<ClientEventHistory> findByUserId(Long userId);
}