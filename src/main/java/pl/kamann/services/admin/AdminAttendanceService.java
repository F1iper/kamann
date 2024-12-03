package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.AttendanceMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.utility.EntityLookupService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService entityLookupService;
    private final AttendanceMapper attendanceMapper;

    public void cancelClientAttendance(Long eventId, Long clientId) {
        Event event = entityLookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByEventAndUserId(event, clientId)
                .orElseThrow(() -> new ApiException(
                        "Attendance not found for event and client",
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND
                ));

        attendanceRepository.delete(attendance);
    }

    public void markAttendance(Long eventId, Long clientId, AttendanceStatus status) {
        Event event = entityLookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByEventAndUserId(event, clientId)
                .orElseThrow(() -> new ApiException(
                        "Attendance not found for event and client",
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND
                ));

        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    public Page<AttendanceDetailsDto> getAttendanceDetails(Long eventId, Long userId, Pageable pageable) {
        Page<Attendance> attendancePage;

        if (eventId != null && userId != null) {
            attendancePage = attendanceRepository.findByEventIdAndUserId(eventId, userId, pageable);
        } else if (eventId != null) {
            attendancePage = attendanceRepository.findByEventId(eventId, pageable);
        } else if (userId != null) {
            attendancePage = attendanceRepository.findByUserId(userId, pageable);
        } else {
            attendancePage = attendanceRepository.findAll(pageable);
        }

        return attendancePage.map(attendanceMapper::toDto);
    }

    public Page<AttendanceDetailsDto> getAttendanceSummary(Pageable pageable) {
        Page<Attendance> attendancePage = attendanceRepository.findAll(pageable);
        return attendancePage.map(attendanceMapper::toDto);
    }

    public Map<String, Object> getAttendanceStatistics(Long eventId, Long userId) {
        if (eventId == null && userId == null) {
            throw new ApiException(
                    "Either eventId or userId must be provided",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT);
        }

        return attendanceRepository.calculateStatistics(eventId, userId);
    }
}
