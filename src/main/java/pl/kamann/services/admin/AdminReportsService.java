package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.reports.AttendanceReportDto;
import pl.kamann.dtos.reports.EventReportDto;
import pl.kamann.dtos.reports.RevenueReportDto;
import pl.kamann.mappers.AttendanceReportMapper;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.mappers.RevenueReportMapper;
import pl.kamann.repositories.admin.AttendanceStatRepository;
import pl.kamann.repositories.admin.EventStatRepository;
import pl.kamann.repositories.admin.RevenueStatRepository;

@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final AttendanceStatRepository attendanceStatRepository;
    private final EventStatRepository eventStatRepository;
    private final RevenueStatRepository revenueStatRepository;
    private final EventReportMapper eventReportMapper;
    private final AttendanceReportMapper attendanceReportMapper;
    private final RevenueReportMapper revenueReportMapper;

    public Page<EventReportDto> getEventReports(Pageable pageable) {
        return eventStatRepository.findAll(pageable)
                .map(eventReportMapper::toDto);
    }

    public Page<AttendanceReportDto> getAttendanceReports(Pageable pageable) {
        return attendanceStatRepository.findAll(pageable)
                .map(attendanceReportMapper::toDto);
    }

    public Page<RevenueReportDto> getRevenueReports(Pageable pageable) {
        return revenueStatRepository.findAll(pageable)
                .map(revenueReportMapper::toDto);
    }
}
