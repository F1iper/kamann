package pl.kamann.attendance.controller.management;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.service.management.ManagementAttendanceService;
import pl.kamann.event.dto.EventDto;

import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final ManagementAttendanceService managementAttendanceService;

    @PostMapping("/events/{eventId}/mark")
    public ResponseEntity<Void> markAttendance(
            @PathVariable Long eventId,
            @RequestParam Long userId,
            @RequestParam AttendanceStatus status) {
        managementAttendanceService.markAttendance(eventId, userId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/cancel")
    public ResponseEntity<Attendance> cancelAttendance(
            @PathVariable Long eventId,
            @RequestParam Long userId) {
        Attendance attendance = managementAttendanceService.cancelAttendance(eventId, userId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto>> getUpcomingEvents(@RequestParam Long userId) {
        List<EventDto> events = managementAttendanceService.getUpcomingEvents(userId);
        return ResponseEntity.ok(events);
    }
}
