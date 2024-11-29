package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("""
            SELECT e FROM Event e
            WHERE e.startTime > :currentTime
            AND SIZE(e.participants) < e.maxParticipants
            AND e.status = pl.kamann.entities.EventStatus.SCHEDULED
            ORDER BY e.startTime ASC
        """)
    List<Event> findAvailableEvents(@Param("currentTime") LocalDateTime currentTime);

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
            WHERE e.startTime > :now
            AND EXISTS (
                SELECT a FROM Attendance a
                WHERE a.event = e AND a.user = :user
            )
        """)
    List<Event> findUpcomingEventsForUser(@Param("user") AppUser user, @Param("now") LocalDateTime now);

    @Query("""
            SELECT e FROM Event e
            WHERE e.instructor.id = :instructorId
            AND e.startTime > :currentTime
            ORDER BY e.startTime ASC
        """)
    List<Event> findUpcomingEventsForInstructor(@Param("instructorId") Long instructorId, @Param("currentTime") LocalDateTime currentTime);

    @Query("""
            SELECT e FROM Event e
            WHERE ((e.startTime BETWEEN :startDate AND :endDate OR 
                    e.endTime BETWEEN :startDate AND :endDate) OR :startDate IS NULL OR :endDate IS NULL) 
            AND (e.instructor.id = :instructorId OR :instructorId IS NULL) 
            AND (e.eventType.name = :eventType OR :eventType IS NULL) 
            AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
                 LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR :keyword IS NULL)
        """)
    Page<Event> findFilteredEvents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("instructorId") Long instructorId,
            @Param("eventType") String eventType,
            @Param("keyword") String keyword,
            Pageable pageable);
}
