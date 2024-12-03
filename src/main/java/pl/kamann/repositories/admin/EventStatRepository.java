package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamann.entities.reports.EventStatEntity;

@Repository
public interface EventStatRepository extends JpaRepository<EventStatEntity, Long> {
}