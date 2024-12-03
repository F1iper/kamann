package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.*;
import pl.kamann.mappers.AttendanceReportMapper;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.mappers.RevenueReportMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.admin.RevenueRepository;

@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final RevenueRepository revenueRepository;
    private final EventReportMapper eventReportMapper;
    private final AttendanceReportMapper attendanceReportMapper;
    private final RevenueReportMapper revenueReportMapper;

    public Page<EventReportDto> getEventReports(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<EventStat> statsPage = eventRepository.findEventStats(pageRequest);
        if (statsPage.isEmpty()) {
            throw new ApiException(
                    "No event statistics found",
                    HttpStatus.NOT_FOUND,
                    Codes.NO_EVENT_STATS
            );
        }
        return statsPage.map(eventReportMapper::toDto);
    }

    public Page<AttendanceReportDto> getAttendanceReports(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<AttendanceStat> statsPage = attendanceRepository.findAttendanceStats(pageRequest);
        if (statsPage.isEmpty()) {
            throw new ApiException(
                    "No attendance statistics found",
                    HttpStatus.NOT_FOUND,
                    Codes.NO_ATTENDANCE_STATS
            );
        }
        return statsPage.map(attendanceReportMapper::toDto);
    }

    public Page<RevenueReportDto> getRevenueReports(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<RevenueStat> statsPage = revenueRepository.findRevenueStats(pageRequest);
        if (statsPage.isEmpty()) {
            throw new ApiException(
                    "No revenue statistics found",
                    HttpStatus.NOT_FOUND,
                    Codes.NO_REVENUE_STATS
            );
        }
        return statsPage.map(revenueReportMapper::toDto);
    }
}
