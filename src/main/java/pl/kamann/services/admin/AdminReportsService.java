package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kamann.entities.reports.AttendanceStatEntity;
import pl.kamann.entities.reports.EventStatEntity;
import pl.kamann.entities.reports.RevenueStatEntity;
import pl.kamann.repositories.admin.AttendanceStatRepository;
import pl.kamann.repositories.admin.EventStatRepository;
import pl.kamann.repositories.admin.RevenueStatRepository;

@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final AttendanceStatRepository attendanceStatRepository;
    private final EventStatRepository eventStatRepository;
    private final RevenueStatRepository revenueStatRepository;

    public Page<EventStatEntity> getEventReports(Pageable pageable) {
        return eventStatRepository.findAll(pageable);
    }

    public Page<AttendanceStatEntity> getAttendanceReports(Pageable pageable) {
        return attendanceStatRepository.findAll(pageable);
    }

    public Page<RevenueStatEntity> getRevenueReports(Pageable pageable) {
        return revenueStatRepository.findAll(pageable);
    }
}
