package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.event.Event;

import java.util.Optional;

public interface UserEventHistoryRepository extends JpaRepository<ClientEventHistory, Long> {

    Optional<ClientEventHistory> findByUserAndEvent(AppUser user, Event event);
}
