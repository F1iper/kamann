package pl.kamann.classschedule.service;

import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.classschedule.model.ClassSchedule;
import pl.kamann.classschedule.repository.ClassScheduleRepository;

import java.util.List;

@Service
public class ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;

    public ClassScheduleService(ClassScheduleRepository classScheduleRepository) {
        this.classScheduleRepository = classScheduleRepository;
    }

    public ClassSchedule addClass(ClassSchedule classSchedule) {
        return classScheduleRepository.save(classSchedule);
    }

    public ClassSchedule updateClass(ClassSchedule classSchedule) {
        return classScheduleRepository.save(classSchedule);
    }

    public void deleteClass(Long id) {
        classScheduleRepository.deleteById(id);
    }

    public List<ClassSchedule> getClassesByInstructor(AppUser instructor) {
        return classScheduleRepository.findByInstructor(instructor);
    }
}
