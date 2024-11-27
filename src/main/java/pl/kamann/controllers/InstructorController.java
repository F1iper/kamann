package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.services.InstructorAttendanceService;
import pl.kamann.config.exception.handler.ApiException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorController {

    private final InstructorAttendanceService attendanceService;

    @PostMapping("/attendance/{eventId}/{clientId}/cancel")
    public ResponseEntity<String> cancelAttendance(@PathVariable Long eventId, @PathVariable Long clientId) {
        attendanceService.cancelAttendanceForClient(eventId, clientId);
        return ResponseEntity.ok("Attendance successfully cancelled by instructor.");
    }

    @PostMapping("/attendance/{eventId}/{clientId}/mark")
    public ResponseEntity<String> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId,
            @RequestParam String status) {
        try {
            AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
            attendanceService.markAttendance(eventId, clientId, attendanceStatus);
            return ResponseEntity.ok("Attendance successfully marked as " + attendanceStatus + ".");
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid attendance status provided.", HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }
    }
}
