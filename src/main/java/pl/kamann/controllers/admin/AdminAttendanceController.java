package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.admin.AdminAttendanceService;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AdminAttendanceService adminAttendanceService;

    @PostMapping("/{eventId}/{clientId}/cancel")
    @Operation(summary = "Cancel client attendance", description = "Cancels the attendance of a client for a specific event.")
    public ResponseEntity<String> cancelAttendance(@PathVariable Long eventId, @PathVariable Long clientId) {
        adminAttendanceService.cancelClientAttendance(eventId, clientId);
        return ResponseEntity.ok("Attendance successfully cancelled by admin.");
    }

    @PostMapping("/{eventId}/{clientId}/mark")
    @Operation(summary = "Mark attendance", description = "Marks the attendance status for a specific client in a specific event.")
    public ResponseEntity<String> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId,
            @RequestParam String status) {
        var attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
        adminAttendanceService.markAttendance(eventId, clientId, attendanceStatus);
        return ResponseEntity.ok("Attendance successfully marked as " + attendanceStatus + ".");
    }
}
