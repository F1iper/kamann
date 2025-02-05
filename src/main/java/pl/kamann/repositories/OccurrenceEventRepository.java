package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface OccurrenceEventRepository extends JpaRepository<OccurrenceEvent, Long> {

    @Query("SELECT e FROM Event e WHERE e.status = 'SCHEDULED' AND e.start > CURRENT_TIMESTAMP")
    List<Event> findAllAvailableEvents();

    @Query("SELECT o FROM OccurrenceEvent o WHERE o.start >= :dateTime AND o.event.createdBy.id <> :userId")
    List<OccurrenceEvent> findAvailableEventsExcludingClient(
            @Param("dateTime") LocalDateTime dateTime,
            @Param("userId") Long userId
    );

    @Query("SELECT o FROM OccurrenceEvent o JOIN o.attendances a " +
            "WHERE a.user.id = :userId AND a.status = 'REGISTERED'")
    List<OccurrenceEvent> findRegisteredOccurrencesByUser(@Param("userId") Long userId);

    List<OccurrenceEvent> findByEventAndStartAfter(Event event, LocalDateTime dateTime);

    boolean existsByEvent(Event event);

    void deleteByEvent(Event event);

    @Modifying
    @Query("DELETE FROM OccurrenceEvent o WHERE o.event.id = :eventId AND o.start >= :dateTime")
    void deleteFutureOccurrences(@Param("eventId") Long eventId, @Param("dateTime") LocalDateTime dateTime);

    Page<OccurrenceEvent> findDistinctByInstructorId(Long instructorId, Pageable pageable);

    @Query("SELECT o FROM OccurrenceEvent o " +
            "WHERE o.start BETWEEN :startDateTime AND :endDateTime")
    List<OccurrenceEvent> findOccurrencesInRange(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
