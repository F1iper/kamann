package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.dtos.AttendanceStat;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
                SELECT new pl.kamann.models.AttendanceStat(
                    a.event.name,
                    COUNT(a),
                    SUM(CASE WHEN a.status = 'ATTENDED' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN a.status = 'LATE_CANCELLATION' THEN 1 ELSE 0 END)
                )
                FROM Attendance a
                GROUP BY a.event.name
            """)
    Page<AttendanceStat> findAttendanceStats(Pageable pageable);

    Optional<Attendance> findByUserAndEvent(AppUser user, Event event);

    List<Attendance> findByEvent(Event event);

    List<Attendance> findByUser(AppUser user);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event = :event AND a.status = :status")
    int countByEventAndStatus(@Param("event") Event event, @Param("status") AttendanceStatus status);

    @Query("SELECT a.event FROM Attendance a WHERE a.user = :user AND a.event.startTime > :now")
    List<Event> findUpcomingEventsForUser(@Param("user") AppUser user, @Param("now") LocalDateTime now);

    @Query("SELECT a.event FROM Attendance a WHERE a.user = :user AND a.event.startTime BETWEEN :start AND :end")
    List<Event> findEventsByUserAndTimeFrame(@Param("user") AppUser user,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    List<Attendance> findByUserEmail(String email);

    @Query("SELECT e FROM Event e WHERE e.instructor.id = :instructorId AND e.startTime > :currentTime")
    List<Event> findUpcomingEventsForInstructor(@Param("instructorId") Long instructorId, @Param("currentTime") LocalDateTime currentTime);

    List<Attendance> findAllByEvent(Event event);

    @Query("SELECT a FROM Attendance a WHERE a.event = :event AND (a.status IS NULL OR a.status = 'REGISTERED')")
    List<Attendance> findUnmarkedOrRegisteredAttendancesByEvent(@Param("event") Event event);

}