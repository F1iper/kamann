package pl.kamann.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.service.AttendanceService;
import pl.kamann.config.global.Codes;

@RestController
@RequestMapping("api/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('" + Codes.ADMIN + "', '" + Codes.INSTRUCTOR + "')")
public class InstructorController {

    private final AttendanceService attendanceService;

    @PostMapping("/attendance/{eventId}/{clientId}/cancel")
    public ResponseEntity<Void> cancelAttendanceForClient(
            @PathVariable Long eventId,
            @PathVariable Long clientId) {
        attendanceService.cancelAttendanceByInstructor(eventId, clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/attendance/{eventId}/{clientId}/mark")
    public ResponseEntity<Void> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long clientId,
            @RequestParam AttendanceStatus status) {
        attendanceService.markAttendance(eventId, clientId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/membership/{clientId}/mark-paid")
    public ResponseEntity<Void> markMembershipAsPaid(@PathVariable Long clientId) {
        attendanceService.markMembershipAsPaid(clientId);
        return ResponseEntity.ok().build();
    }
}
