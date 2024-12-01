package pl.kamann.controllers.instructor;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.instructor.InstructorAttendanceService;

@RestController
@RequestMapping("/api/instructor/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorAttendanceController {

    private final InstructorAttendanceService instructorAttendanceService;

    @Operation(summary = "Cancel client attendance", description = "Cancels the attendance of a client for a specific event.")
    @PostMapping("/{eventId}/{clientId}/cancel")
    public ResponseEntity<String> cancelAttendance(@PathVariable Long eventId, @PathVariable Long clientId) {
        instructorAttendanceService.cancelClientAttendance(eventId, clientId);
        return ResponseEntity.ok("Attendance successfully cancelled by instructor.");
    }

    @Operation(summary = "Mark attendance", description = "Marks the attendance status for a specific client in a specific event.")
    @PostMapping("/{eventId}/{clientId}/mark")
    public ResponseEntity<String> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId,
            @RequestParam String status) {
        var attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
        instructorAttendanceService.markAttendance(eventId, clientId, attendanceStatus);
        return ResponseEntity.ok("Attendance successfully marked as " + attendanceStatus + ".");
    }
}
