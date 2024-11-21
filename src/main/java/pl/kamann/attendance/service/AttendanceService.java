package pl.kamann.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.event.model.Event;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final AppUserRepository appUserRepository;

    public Attendance markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> new Attendance(null, user, event, status, LocalDateTime.now()));

        attendance.setStatus(status);
        attendance.setTimestamp(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Attendance cancelEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AttendanceStatus status = LocalDateTime.now().isBefore(event.getStartTime().minusHours(24))
                ? AttendanceStatus.EARLY_CANCEL
                : AttendanceStatus.LATE_CANCEL;

        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> new Attendance(null, user, event, status, LocalDateTime.now()));

        attendance.setStatus(status);
        attendance.setTimestamp(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getEventAttendance(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return attendanceRepository.findByEvent(event);
    }

    public List<Attendance> getUserAttendance(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return attendanceRepository.findByUser(user);
    }
}