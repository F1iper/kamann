package pl.kamann.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.repository.AppUserRepository;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.classschedule.model.ClassSchedule;
import pl.kamann.classschedule.repository.ClassScheduleRepository;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AppUserRepository appUserRepository;


    public Attendance recordAttendance(Long classScheduleId, Long participantId, AttendanceStatus status) {
        ClassSchedule classSchedule = classScheduleRepository.findById(classScheduleId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        AppUser participant = appUserRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Attendance attendance = new Attendance();
        attendance.setClassSchedule(classSchedule);
        attendance.setParticipant(participant);
        attendance.setAttendanceStatus(status);

        return attendanceRepository.save(attendance);
    }
}
