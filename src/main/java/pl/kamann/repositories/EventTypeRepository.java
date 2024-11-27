package pl.kamann.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.EventType;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    Optional<EventType> findByName(String name);

    boolean existsByName(String eventTypeName);
}