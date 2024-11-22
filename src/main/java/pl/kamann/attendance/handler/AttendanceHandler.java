package pl.kamann.attendance.handler;

import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.event.dto.EventDto;

import java.util.List;

public interface AttendanceHandler {
    void markAttendance(Long eventId, Long userId, AttendanceStatus status);
    Attendance cancelAttendance(Long eventId, Long userId);
    List<EventDto> getUpcomingEvents(Long userId);
}