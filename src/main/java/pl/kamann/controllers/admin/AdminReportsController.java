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
import pl.kamann.dtos.EventReportDto;
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
        Page<EventReportDto> reports = adminReportsService.getEventReports(pageable);
        return ResponseEntity.ok(reports);
    }
}
