package pl.kamann.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.event.model.Event;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    int countByEventAndStatus(Event event, AttendanceStatus status);
}