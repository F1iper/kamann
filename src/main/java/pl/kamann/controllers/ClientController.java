package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.Attendance;
import pl.kamann.entities.EventDto;
import pl.kamann.services.ClientAttendanceService;
import pl.kamann.services.EventService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final EventService eventService;
    private final ClientAttendanceService attendanceService;
    private final ClientAttendanceService clientAttendanceService;

    // Events
    @GetMapping("/events/upcoming")
    public ResponseEntity<List<EventDto>> getUpcomingEvents() {
        List<EventDto> events = eventService.getUpcomingEventsForLoggedInClient();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<String> joinEvent(@PathVariable Long eventId) {
        try {
            Attendance attendance = clientAttendanceService.joinEvent(eventId);
            return ResponseEntity.ok("Successfully joined the event.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to join the event: " + e.getMessage());
        }
    }

    // Attendances
    @PostMapping("/attendance/events/{eventId}/cancel")
    public ResponseEntity<String> cancelAttendance(@PathVariable Long eventId) {
        try {
            Attendance attendance = attendanceService.cancelAttendanceForClient(eventId);
            return ResponseEntity.ok().body("Attendance for event: " + attendance.getEvent().getTitle() + " successfully cancelled.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel attendance: " + e.getMessage());
        }
    }

    @GetMapping("/attendance/summary")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary() {
        Map<String, Object> summary = attendanceService.getAttendanceSummary();
        return ResponseEntity.ok(summary);
    }
}
