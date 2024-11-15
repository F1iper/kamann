package pl.kamann.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.classschedule.model.ClassSchedule;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByClassSchedule(ClassSchedule classSchedule);
}
