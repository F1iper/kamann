package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM OccurrenceEvent e JOIN e.attendances a WHERE a.user = :user AND a.status = :status")
    List<OccurrenceEvent> findEventsByUserAndStatus(@Param("user") AppUser user, @Param("status") AttendanceStatus status);

    @Query("SELECT e FROM Event e WHERE e.instructor = :instructor")
    Page<Event> findByInstructor(@Param("instructor") AppUser instructor, Pageable pageable);

}
