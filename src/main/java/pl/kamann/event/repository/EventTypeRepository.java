package pl.kamann.event.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.event.model.EventType;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    Optional<EventType> findByName(String name);
}