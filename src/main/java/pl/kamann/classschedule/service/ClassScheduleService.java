package pl.kamann.classschedule.service;

import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.repository.AppUserRepository;
import pl.kamann.classschedule.model.ClassSchedule;
import pl.kamann.classschedule.repository.ClassScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;
    private final AppUserRepository appUserRepository;

    public ClassScheduleService(ClassScheduleRepository classScheduleRepository, AppUserRepository appUserRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.appUserRepository = appUserRepository;
    }

    public ClassSchedule createClassSchedule(String title, LocalDateTime startTime, LocalDateTime endTime, Long instructorId, int maxParticipants) {
        AppUser instructor = appUserRepository.findById(instructorId).orElseThrow(() -> new RuntimeException("Instructor not found"));
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setTitle(title);
        classSchedule.setStartTime(startTime);
        classSchedule.setEndTime(endTime);
        classSchedule.setInstructor(instructor);
        classSchedule.setMaxParticipants(maxParticipants);

        return classScheduleRepository.save(classSchedule);
    }
}
