package pl.kamann.services.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.*;
import pl.kamann.mappers.AttendanceReportMapper;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.mappers.RevenueReportMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.admin.RevenueRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AdminReportsServiceTest {

    private AdminReportsService adminReportsService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private RevenueRepository revenueRepository;

    @Mock
    private EventReportMapper eventReportMapper;

    @Mock
    private RevenueReportMapper revenueReportMapper;

    @Mock
    private AttendanceReportMapper attendanceReportMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminReportsService = new AdminReportsService(
                eventRepository,
                attendanceRepository,
                revenueRepository,
                eventReportMapper,
                attendanceReportMapper,
                revenueReportMapper
        );
    }

    @Test
    void testGetEventReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals("Page index must not be less than zero", exception.getMessage());
    }

    @Test
    void testGetEventReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -10));
        assertEquals("Page size must not be less than one", exception.getMessage());
    }

    @Test
    void testGetEventReportsEmptyPageRequest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, 0));
        assertEquals("Page size must not be less than one", exception.getMessage());
    }

    @Test
    void testGetEventReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<EventStat> mockStats = List.of(
                new EventStat("Yoga", 10, 7, 3),
                new EventStat("Dance", 20, 15, 5)
        );
        Page<EventStat> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(eventRepository.findEventStats(pageable)).thenReturn(mockPage);
        when(eventReportMapper.toDto(mockStats.get(0)))
                .thenReturn(new EventReportDto("Yoga", 10, 7, 3));
        when(eventReportMapper.toDto(mockStats.get(1)))
                .thenReturn(new EventReportDto("Dance", 20, 15, 5));

        Page<EventReportDto> result = adminReportsService.getEventReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).getEventType());
        assertEquals(10, result.getContent().get(0).getTotalEvents());
        assertEquals("Dance", result.getContent().get(1).getEventType());
        assertEquals(20, result.getContent().get(1).getTotalEvents());
    }

    @Test
    void testGetEventReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<EventStat> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(eventRepository.findEventStats(pageable)).thenReturn(mockPage);

        ApiException exception = assertThrows(ApiException.class, () -> adminReportsService.getEventReports(pageable));
        assertEquals("No event statistics found", exception.getMessage());
        assertEquals(Codes.NO_EVENT_STATS, exception.getCode());
    }

    @Test
    void testGetAttendanceReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<AttendanceStat> mockStats = List.of(
                new AttendanceStat("Yoga", 10, 7, 2, 1),
                new AttendanceStat("Dance", 20, 15, 4, 1)
        );
        Page<AttendanceStat> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(attendanceRepository.findAttendanceStats(pageable)).thenReturn(mockPage);
        when(attendanceReportMapper.toDto(mockStats.get(0)))
                .thenReturn(new AttendanceReportDto("Yoga", 10, 7, 2, 1));
        when(attendanceReportMapper.toDto(mockStats.get(1)))
                .thenReturn(new AttendanceReportDto("Dance", 20, 15, 4, 1));

        Page<AttendanceReportDto> result = adminReportsService.getAttendanceReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).getEventName());
        assertEquals(10, result.getContent().get(0).getTotalParticipants());
        assertEquals("Dance", result.getContent().get(1).getEventName());
        assertEquals(20, result.getContent().get(1).getTotalParticipants());
    }

    @Test
    void testGetAttendanceReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<AttendanceStat> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(attendanceRepository.findAttendanceStats(pageable)).thenReturn(mockPage);

        ApiException exception = assertThrows(ApiException.class, () -> adminReportsService.getAttendanceReports(pageable));
        assertEquals("No attendance statistics found", exception.getMessage());
        assertEquals(Codes.NO_ATTENDANCE_STATS, exception.getCode());
    }

    @Test
    void testGetAttendanceReportsLargePageSizeSmallDataset() {
        Pageable pageable = PageRequest.of(0, 50);
        List<AttendanceStat> mockStats = List.of(new AttendanceStat("Yoga", 10, 7, 2, 1));
        Page<AttendanceStat> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(attendanceRepository.findAttendanceStats(pageable)).thenReturn(mockPage);
        when(attendanceReportMapper.toDto(mockStats.get(0)))
                .thenReturn(new AttendanceReportDto("Yoga", 10, 7, 2, 1));

        Page<AttendanceReportDto> result = adminReportsService.getAttendanceReports(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).getEventName());
    }

    @Test
    void testGetRevenueReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<RevenueStat> mockStats = List.of(
                new RevenueStat("Basic", 2000.0, 10),
                new RevenueStat("Premium", 5000.0, 20)
        );
        Page<RevenueStat> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(revenueRepository.findRevenueStats(pageable)).thenReturn(mockPage);
        when(revenueReportMapper.toDto(mockStats.get(0)))
                .thenReturn(new RevenueReportDto("Basic", 2000.0, 10));
        when(revenueReportMapper.toDto(mockStats.get(1)))
                .thenReturn(new RevenueReportDto("Premium", 5000.0, 20));

        Page<RevenueReportDto> result = adminReportsService.getRevenueReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Basic", result.getContent().get(0).getMembershipType());
        assertEquals(2000.0, result.getContent().get(0).getTotalRevenue());
        assertEquals("Premium", result.getContent().get(1).getMembershipType());
        assertEquals(5000.0, result.getContent().get(1).getTotalRevenue());
    }

    @Test
    void testGetRevenueReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<RevenueStat> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(revenueRepository.findRevenueStats(pageable)).thenReturn(mockPage);

        ApiException exception = assertThrows(ApiException.class, () -> adminReportsService.getRevenueReports(pageable));
        assertEquals("No revenue statistics found", exception.getMessage());
        assertEquals(Codes.NO_REVENUE_STATS, exception.getCode());
    }

    @Test
    void testGetRevenueReportsLargePageSizeSmallDataset() {
        Pageable pageable = PageRequest.of(0, 50);
        List<RevenueStat> mockStats = List.of(new RevenueStat("Basic", 2000.0, 10));
        Page<RevenueStat> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(revenueRepository.findRevenueStats(pageable)).thenReturn(mockPage);
        when(revenueReportMapper.toDto(mockStats.get(0)))
                .thenReturn(new RevenueReportDto("Basic", 2000.0, 10));

        Page<RevenueReportDto> result = adminReportsService.getRevenueReports(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Basic", result.getContent().get(0).getMembershipType());
        assertEquals(2000.0, result.getContent().get(0).getTotalRevenue());
    }

}
