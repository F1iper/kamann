package pl.kamann.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.event.EventType;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
}