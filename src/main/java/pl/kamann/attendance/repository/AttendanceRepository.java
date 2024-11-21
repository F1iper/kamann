package pl.kamann.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndEvent(AppUser user, Event event);

    List<Attendance> findByEvent(Event event);

    List<Attendance> findByUser(AppUser user);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event = :event AND a.status = :status")
    int countByEventAndStatus(@Param("event") Event event, @Param("status") AttendanceStatus status);
}