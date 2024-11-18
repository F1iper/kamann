package pl.kamann.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}