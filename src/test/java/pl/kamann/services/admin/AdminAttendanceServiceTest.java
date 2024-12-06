package pl.kamann.services.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.AttendanceMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminAttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EntityLookupService entityLookupService;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AdminAttendanceService adminAttendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cancelClientAttendance_ShouldDeleteAttendance_WhenFound() {
        Long eventId = 1L;
        Long clientId = 2L;

        Event event = new Event();
        Attendance attendance = new Attendance();

        when(entityLookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByEventAndUserId(event, clientId)).thenReturn(Optional.of(attendance));

        adminAttendanceService.cancelClientAttendance(eventId, clientId);

        verify(attendanceRepository).delete(attendance);
    }

    @Test
    void cancelClientAttendance_ShouldThrowException_WhenNotFound() {
        Long eventId = 1L;
        Long clientId = 2L;

        Event event = new Event();

        when(entityLookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByEventAndUserId(event, clientId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> adminAttendanceService.cancelClientAttendance(eventId, clientId));

        assertEquals("Attendance not found for event and client", exception.getMessage());
        verify(attendanceRepository, never()).delete(any());
    }

    @Test
    void markAttendance_ShouldUpdateStatus_WhenFound() {
        var eventId = 1L;
        var clientId = 2L;
        var status = AttendanceStatus.PRESENT;

        var event = new Event();
        var attendance = new Attendance();

        when(entityLookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByEventAndUserId(event, clientId)).thenReturn(Optional.of(attendance));

        adminAttendanceService.markAttendance(eventId, clientId, status);

        assertEquals(status, attendance.getStatus());
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void markAttendance_ShouldThrowException_WhenNotFound() {
        var eventId = 1L;
        var clientId = 2L;

        var event = new Event();

        when(entityLookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByEventAndUserId(event, clientId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> adminAttendanceService.markAttendance(eventId, clientId, AttendanceStatus.PRESENT));

        assertEquals("Attendance not found for event and client", exception.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void getAttendanceDetails_ShouldReturnPage_WhenFound() {
        var eventId = 1L;
        var userId = 2L;
        var pageable = Pageable.unpaged();

        var attendance = new Attendance();
        var dto = AttendanceDetailsDto.builder()
                .id(1L)
                .eventId(10L)
                .userId(100L)
                .status(AttendanceStatus.PRESENT)
                .build();
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance));

        when(attendanceRepository.findByEventIdAndUserId(eventId, userId, pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(dto);

        Page<AttendanceDetailsDto> result = adminAttendanceService.getAttendanceDetails(eventId, userId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }

    @Test
    void getAttendanceSummary_ShouldReturnSummary() {
        var pageable = Pageable.unpaged();

        var attendance = new Attendance();
        var dto = AttendanceDetailsDto.builder()
                .id(1L)
                .eventId(10L)
                .userId(100L)
                .status(AttendanceStatus.PRESENT)
                .build();
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance));

        when(attendanceRepository.findAll(pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(dto);

        Page<AttendanceDetailsDto> result = adminAttendanceService.getAttendanceSummary(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }

    @Test
    void getAttendanceStatistics_ShouldReturnStatistics_WhenValid() {
        var eventId = 1L;
        var userId = 2L;

        Map<String, Object> mockStats = Map.of("total", 10, "present", 7, "absent", 3);
        when(attendanceRepository.calculateStatistics(eventId, userId)).thenReturn(mockStats);

        Map<String, Object> result = adminAttendanceService.getAttendanceStatistics(eventId, userId);

        assertEquals(mockStats, result);
    }

    @Test
    void getAttendanceStatistics_ShouldThrowException_WhenNoInputProvided() {
        var exception = assertThrows(ApiException.class,
                () -> adminAttendanceService.getAttendanceStatistics(null, null));

        assertEquals("Either eventId or userId must be provided", exception.getMessage());
    }
}
