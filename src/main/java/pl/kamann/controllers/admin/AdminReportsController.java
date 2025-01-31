package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.dtos.reports.AttendanceReportDto;
import pl.kamann.dtos.reports.EventReportDto;
import pl.kamann.dtos.reports.RevenueReportDto;
import pl.kamann.services.admin.AdminReportsService;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportsController {

    private final AdminReportsService adminReportsService;

    @GetMapping("/events")
    @Operation(summary = "Get event reports with pagination", description = "Retrieves a paginated report of all events with types, completion, and cancellation stats.")
    public ResponseEntity<Page<EventReportDto>> getEventReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportsService.getEventReports(pageable));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Get attendance reports with pagination", description = "Retrieves a paginated report of attendance rates, absence trends, and late cancellations.")
    public ResponseEntity<Page<AttendanceReportDto>> getAttendanceReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportsService.getAttendanceReports(pageable));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue reports with pagination", description = "Retrieves a paginated report of revenue data by membership type and total revenue.")
    public ResponseEntity<Page<RevenueReportDto>> getRevenueReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportsService.getRevenueReports(pageable));
    }
}
