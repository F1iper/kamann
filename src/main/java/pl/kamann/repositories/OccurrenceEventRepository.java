package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface OccurrenceEventRepository extends JpaRepository<OccurrenceEvent, Long> {

    @Query("SELECT o FROM OccurrenceEvent o WHERE o.date >= :date AND o.startTime >= :time AND o.event.createdBy.id <> :userId")
    List<OccurrenceEvent> findAvailableEventsExcludingClient(@Param("date") LocalDate date, @Param("time") LocalTime time, @Param("userId") Long userId);

    @Query("SELECT o FROM OccurrenceEvent o JOIN o.attendances a WHERE a.user.id = :userId AND a.status = 'REGISTERED'")
    List<OccurrenceEvent> findRegisteredOccurrencesByUser(@Param("userId") Long userId);

//    @Query("""
//    SELECT e FROM OccurrenceEvent e
//    WHERE (e.date > :nowDate OR (e.date = :nowDate AND e.startTime > :nowTime))
//    AND e.id NOT IN (
//        SELECT a.occurrenceEvent.id FROM Attendance a
//        WHERE a.user.id = :clientId
//    )
//    ORDER BY e.date ASC, e.startTime ASC
//""")
//    List<OccurrenceEvent> findAvailableEventsExcludingClient(
//            @Param("nowDate") LocalDate nowDate,
//            @Param("nowTime") LocalTime nowTime,
//            @Param("clientId") Long clientId);

    List<OccurrenceEvent> findByEventAndDateAfter(Event event, LocalDate date);

    boolean existsByEvent(Event event);

    void deleteByEvent(Event event);

    Page<OccurrenceEvent> findDistinctByInstructorId(Long instructorId, Pageable validatedPageable);

    List<OccurrenceEvent> findByEventId(Long eventId);
}
