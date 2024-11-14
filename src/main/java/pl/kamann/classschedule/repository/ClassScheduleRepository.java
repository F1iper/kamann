package pl.kamann.classschedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.classschedule.model.ClassSchedule;

import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByInstructor(AppUser instructor);
}
