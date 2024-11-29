package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.Attendance;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.services.ClientAttendanceService;
import pl.kamann.services.InstructorAttendanceService;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final ClientAttendanceService clientAttendanceService;
    private final InstructorAttendanceService instructorAttendanceService;

    @PostMapping("/{eventId}/join")
    public ResponseEntity<Attendance> joinEvent(@PathVariable Long eventId) {
        Attendance attendance = clientAttendanceService.joinEvent(eventId);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelAttendanceForClient(@PathVariable Long eventId) {
        clientAttendanceService.cancelAttendanceForClient(eventId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{eventId}/mark")
    public ResponseEntity<Void> markAttendance(@PathVariable Long eventId,
                                                @RequestParam Long userId,
                                                @RequestParam AttendanceStatus status) {
        instructorAttendanceService.markAttendance(eventId, userId, status);
        return ResponseEntity.ok().build();
    }
}
