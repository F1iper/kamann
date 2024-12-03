package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.dtos.EventStat;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
        SELECT new pl.kamann.models.EventStat(
            e.eventType,
            COUNT(e),
            SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN e.status = 'CANCELLED' THEN 1 ELSE 0 END)
        )
        FROM Event e
        GROUP BY e.eventType
    """)
    Page<EventStat> findEventStats(Pageable pageable);

    @Query("SELECT e FROM Event e JOIN e.attendances a WHERE a.user = :user AND a.status = :status")
    List<Event> findEventsByUserAndStatus(@Param("user") AppUser user, @Param("status") AttendanceStatus status);

    @Query("SELECT e FROM Event e JOIN e.attendances a WHERE a.user = :user")
    List<Event> findAllEventsByUser(@Param("user") AppUser user);

    @Query("""
                SELECT e FROM Event e
                WHERE e.instructor.id = :instructorId
                AND e.startTime > :currentTime
                ORDER BY e.startTime ASC
            """)
    List<Event> findUpcomingEventsForInstructor(@Param("instructorId") Long instructorId, @Param("currentTime") LocalDateTime currentTime);

    @Query("""
                SELECT e FROM Event e 
                WHERE e.startTime > :now 
                AND e.id NOT IN (
                    SELECT a.event.id FROM Attendance a 
                    WHERE a.user.id = :clientId
                )
            """)
    List<Event> findAvailableEventsExcludingClient(@Param("now") LocalDateTime now, @Param("clientId") Long clientId);
}
