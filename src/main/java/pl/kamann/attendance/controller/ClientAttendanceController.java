package pl.kamann.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.service.AttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/user/attendance")
@RequiredArgsConstructor
public class ClientAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/events/{eventId}/cancel")
    public ResponseEntity<Attendance> cancelEvent(@PathVariable Long eventId, @RequestParam Long userId) {
        Attendance attendance = attendanceService.cancelEvent(userId, eventId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> getUserAttendance(@RequestParam Long userId) {
        List<Attendance> attendanceList = attendanceService.getUserAttendance(userId);
        return ResponseEntity.ok(attendanceList);
    }
}