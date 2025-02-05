package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OccurrenceEventRepository extends JpaRepository<OccurrenceEvent, Long> {

    Optional<OccurrenceEvent> findByEventIdAndStart(Long eventId, LocalDateTime start);

    List<OccurrenceEvent> findByStartAfterAndCanceledFalse(LocalDateTime now);

    List<OccurrenceEvent> findAllByEventId(Long eventId);

    List<OccurrenceEvent> findByParticipants_IdAndCanceledFalse(Long userId);

    List<OccurrenceEvent> findByStartBeforeAndCanceledFalse(LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.status = 'SCHEDULED' AND e.start > CURRENT_TIMESTAMP")
    List<Event> findAllAvailableEvents();

    List<OccurrenceEvent> findByEventAndStartAfter(Event event, LocalDateTime dateTime);

    boolean existsByEvent(Event event);

    void deleteByEvent(Event event);

    Page<OccurrenceEvent> findDistinctByInstructorId(Long instructorId, Pageable pageable);

}
