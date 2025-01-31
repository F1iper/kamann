package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN e.attendances a WHERE a.user = :user AND a.status = :status")
    List<Event> findEventsByUserAndStatus(@Param("user") AppUser user, @Param("status") AttendanceStatus status);

    @Query("""
                SELECT e FROM Event e 
                WHERE e.instructor.id = :instructorId 
                AND (e.startDate > :currentDate OR (e.startDate = :currentDate AND e.time > :currentTime))
                ORDER BY e.startDate ASC, e.time ASC
            """)
    List<Event> findUpcomingEventsForInstructor(
            @Param("instructorId") Long instructorId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime);


    @Query("""
                SELECT e FROM Event e
                LEFT JOIN FETCH e.createdBy
                LEFT JOIN FETCH e.instructor
                LEFT JOIN FETCH e.eventType
                WHERE (e.startDate > :nowDate OR (e.startDate = :nowDate AND e.time > :nowTime))
                AND e.id NOT IN (
                    SELECT a.event.id FROM Attendance a 
                    WHERE a.user.id = :clientId
                )
                ORDER BY e.startDate ASC, e.time ASC
            """)
    List<Event> findAvailableEventsExcludingClient(
            @Param("nowDate") LocalDate nowDate,
            @Param("nowTime") LocalTime nowTime,
            @Param("clientId") Long clientId);

    @Query("SELECT e FROM Event e WHERE e.instructor = :instructor")
    Page<Event> findByInstructor(@Param("instructor") AppUser instructor, Pageable pageable);

}
