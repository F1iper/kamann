package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndOccurrenceEvent(AppUser appUser, OccurrenceEvent occurrenceEvent);

    Optional<Attendance> findByUserAndOccurrenceEventEvent(AppUser user, Event event);  // Updated method

    @Query("""
        SELECT new map(
            COUNT(a) as totalAttendance,
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount
        )
        FROM Attendance a
        WHERE (:eventId IS NULL OR a.occurrenceEvent.event.id = :eventId)
        AND (:userId IS NULL OR a.user.id = :userId)
    """)
    Map<String, Object> calculateStatistics(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("""
        SELECT a 
        FROM Attendance a 
        WHERE a.occurrenceEvent.event.id = :eventId 
        AND a.occurrenceEvent.instructor.id = :instructorId
    """)
    List<Attendance> findAllByEventAndInstructor(@Param("eventId") Long eventId, @Param("instructorId") Long instructorId);

    @Query("""
        SELECT a 
        FROM Attendance a 
        WHERE a.occurrenceEvent = :occurrenceEvent 
        AND a.user.id = :userId
    """)
    Optional<Attendance> findByOccurrenceEventAndUserId(@Param("occurrenceEvent") OccurrenceEvent occurrenceEvent, @Param("userId") Long userId);

    Page<Attendance> findByOccurrenceEventEventIdAndUserId(Long eventId, Long userId, Pageable pageable);

    Page<Attendance> findByUserId(Long userId, Pageable pageable);

}
