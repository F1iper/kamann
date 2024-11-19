package pl.kamann.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.attendance.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}