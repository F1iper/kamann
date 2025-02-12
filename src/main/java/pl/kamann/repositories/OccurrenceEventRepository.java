package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface OccurrenceEventRepository extends JpaRepository<OccurrenceEvent, Long> {

    boolean existsByEventIdAndStartAfter(Long eventId, LocalDateTime startDate);

    @Query("SELECT o.start FROM OccurrenceEvent o WHERE o.event.id = :eventId")
    List<LocalDateTime> findStartDatesByEventId(@Param("eventId") Long eventId);

    List<OccurrenceEvent> findOccurrencesByEventId(Long eventId);

    List<OccurrenceEvent> findByEventAndStartAfter(Event event, LocalDateTime dateTime);

    boolean existsByEvent(Event event);

    void deleteByEvent(Event event);

    @Query("""
            SELECT o FROM OccurrenceEvent o
            WHERE (:scope = 'UPCOMING' AND o.start >= CURRENT_TIMESTAMP AND :user MEMBER OF o.participants)
               OR (:scope = 'AVAILABLE' AND o.start >= CURRENT_TIMESTAMP AND :user NOT MEMBER OF o.participants)
               OR (:scope = 'PAST' AND o.start < CURRENT_TIMESTAMP)
            """)
    Page<OccurrenceEvent> findFilteredOccurrences(
            @Param("scope") String scope,
            @Param("user") AppUser user,
            Pageable pageable
    );

}
