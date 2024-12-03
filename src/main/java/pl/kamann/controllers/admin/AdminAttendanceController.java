package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.admin.AdminAttendanceService;

import java.util.Map;

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

    @GetMapping("/details")
    @Operation(summary = "View attendance details", description = "View detailed attendance for a specific event or user.")
    public ResponseEntity<Page<AttendanceDetailsDto>> getAttendanceDetails(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        var details = adminAttendanceService.getAttendanceDetails(eventId, userId, pageable);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get attendance summary", description = "Retrieves a summary of attendance for analytics.")
    public ResponseEntity<Page<AttendanceDetailsDto>> getAttendanceSummary(Pageable pageable) {
        var summary = adminAttendanceService.getAttendanceSummary(pageable);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get attendance statistics", description = "Retrieves attendance statistics for a specific event or user.")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId) {
        var statistics = adminAttendanceService.getAttendanceStatistics(eventId, userId);
        return ResponseEntity.ok(statistics);
    }
}
