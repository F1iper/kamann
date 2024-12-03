package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.Event;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndEvent(AppUser user, Event event);

    @Query("""
                SELECT new map(
                    COUNT(a) as totalAttendance,
                    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount,
                    SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount
                )
                FROM Attendance a
                WHERE (:eventId IS NULL OR a.event.id = :eventId)
                AND (:userId IS NULL OR a.user.id = :userId)
            """)
    Map<String, Object> calculateStatistics(@Param("eventId") Long eventId, @Param("userId") Long userId);


    @Query("SELECT a FROM Attendance a WHERE a.event = :event AND a.user.id = :userId")
    Optional<Attendance> findByEventAndUserId(@Param("event") Event event, @Param("userId") Long userId);

    Page<Attendance> findByEventId(Long eventId, Pageable pageable);

    Page<Attendance> findByUserId(Long userId, Pageable pageable);

    Page<Attendance> findByEventIdAndUserId(Long eventId, Long userId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.event = :event AND (a.status IS NULL OR a.status = 'REGISTERED')")
    List<Attendance> findUnmarkedOrRegisteredAttendancesByEvent(@Param("event") Event event);

}