package pl.kamann.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByInstructorAndStartTimeBetween(
            AppUser instructor,
            LocalDateTime start,
            LocalDateTime end
    );
}