package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.kamann.dtos.EventStat;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventStat, Long> {

    @Query("""
        SELECT new pl.kamann.dtos.EventStat(
            e.eventType,
            COUNT(e),
            SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN e.status = 'CANCELLED' THEN 1 ELSE 0 END)
        )
        FROM Event e
        GROUP BY e.eventType
    """)
    List<EventStat> findEventStats();
}
