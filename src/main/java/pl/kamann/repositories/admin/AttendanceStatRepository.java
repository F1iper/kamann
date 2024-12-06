package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.reports.AttendanceStatEntity;

public interface AttendanceStatRepository extends JpaRepository<AttendanceStatEntity, Long> {
}
