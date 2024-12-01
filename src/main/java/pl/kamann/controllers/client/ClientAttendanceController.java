package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.services.client.ClientAttendanceService;

import java.util.Map;

@RestController
@RequestMapping("/api/client/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientAttendanceController {

    private final ClientAttendanceService clientAttendanceService;

    @PostMapping("/{eventId}/join")
    @Operation(summary = "Join an event", description = "Registers the logged-in client to the specified event.")
    public ResponseEntity<Attendance> joinEvent(@PathVariable Long eventId) {
        var attendance = clientAttendanceService.joinEvent(eventId);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel attendance", description = "Cancels the client's attendance for the specified event.")
    public ResponseEntity<String> cancelAttendance(@PathVariable Long eventId) {
        var attendance = clientAttendanceService.cancelAttendance(eventId);
        return ResponseEntity.ok("Attendance for event: " + attendance.getEvent().getTitle() + " successfully cancelled.");
    }

    @GetMapping("/summary")
    @Operation(summary = "Get attendance summary", description = "Retrieves the attendance summary for the logged-in client.")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary() {
        Map<String, Object> summary = clientAttendanceService.getAttendanceSummary();
        return ResponseEntity.ok(summary);
    }
}
