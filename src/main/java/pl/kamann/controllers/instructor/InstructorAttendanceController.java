package pl.kamann.controllers.instructor;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.instructor.InstructorAttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorAttendanceController {

    private final InstructorAttendanceService instructorAttendanceService;

    @PostMapping("/{eventId}/{clientId}/cancel")
    @Operation(summary = "Cancel client attendance", description = "Cancels the attendance of a client for a specific event.")
    public ResponseEntity<String> cancelAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId) {
        instructorAttendanceService.cancelClientAttendance(eventId, clientId);
        return ResponseEntity.ok("Attendance successfully cancelled by instructor.");
    }

    @PostMapping("/{eventId}/{clientId}/mark")
    @Operation(summary = "Mark attendance", description = "Marks the attendance status for a specific client in a specific event.")
    public ResponseEntity<String> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId,
            @RequestParam AttendanceStatus status) {
        instructorAttendanceService.markAttendance(eventId, clientId, status);
        return ResponseEntity.ok("Attendance successfully marked as " + status + ".");
    }

    @GetMapping("/{eventId}/list")
    @Operation(summary = "List attendances for an event", description = "Lists all attendances for a specific event the instructor is assigned to.")
    public ResponseEntity<List<Attendance>> listAttendancesForEvent(@PathVariable Long eventId) {
        var attendances = instructorAttendanceService.listAttendancesForEvent(eventId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/{eventId}/{clientId}/details")
    @Operation(summary = "Get attendance details", description = "Fetches detailed attendance information for a client in a specific event.")
    public ResponseEntity<Attendance> getAttendanceDetails(
            @PathVariable Long eventId,
            @PathVariable Long clientId) {
        var attendance = instructorAttendanceService.getAttendanceDetails(eventId, clientId);
        return ResponseEntity.ok(attendance);
    }
}
