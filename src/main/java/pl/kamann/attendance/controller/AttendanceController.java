package pl.kamann.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.service.AttendanceService;
import pl.kamann.config.global.Codes;

import java.util.List;

@PreAuthorize("hasRole('" + Codes.ADMIN + "') or hasRole('" + Codes.INSTRUCTOR + "')")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;


    @PostMapping("/events/{eventId}/users/{userId}")
    public ResponseEntity<Void> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @RequestParam AttendanceStatus status) {
        attendanceService.markAttendance(eventId, userId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<Attendance>> getEventAttendance(@PathVariable Long eventId) {
        List<Attendance> attendanceList = attendanceService.getEventAttendance(eventId);
        return ResponseEntity.ok(attendanceList);
    }
}