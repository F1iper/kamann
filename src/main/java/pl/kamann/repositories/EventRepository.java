package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
                SELECT e FROM Event e
                WHERE EXISTS (
                    SELECT a FROM Attendance a
                    WHERE a.event = e AND a.user = :user AND a.status = pl.kamann.entities.AttendanceStatus.REGISTERED
                )
            """)
    List<Event> findRegisteredEvents(@Param("user") AppUser user);

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
