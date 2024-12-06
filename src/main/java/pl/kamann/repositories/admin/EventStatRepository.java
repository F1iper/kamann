package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.reports.EventStatEntity;

public interface EventStatRepository extends JpaRepository<EventStatEntity, Long> {
}