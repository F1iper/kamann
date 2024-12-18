package pl.kamann.services.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kamann.config.codes.PaginationCodes;
import pl.kamann.dtos.reports.AttendanceReportDto;
import pl.kamann.dtos.reports.EventReportDto;
import pl.kamann.dtos.reports.RevenueReportDto;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.entities.reports.AttendanceStatEntity;
import pl.kamann.entities.reports.EventStatEntity;
import pl.kamann.entities.reports.RevenueStatEntity;
import pl.kamann.mappers.AttendanceReportMapper;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.mappers.RevenueReportMapper;
import pl.kamann.repositories.admin.AttendanceStatRepository;
import pl.kamann.repositories.admin.EventStatRepository;
import pl.kamann.repositories.admin.RevenueStatRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AdminReportsServiceTest {

    private AdminReportsService adminReportsService;

    @Mock
    private AttendanceStatRepository attendanceStatRepository;

    @Mock
    private EventStatRepository eventStatRepository;

    @Mock
    private RevenueStatRepository revenueStatRepository;

    @Mock
    private EventReportMapper eventReportMapper;

    @Mock
    private AttendanceReportMapper attendanceReportMapper;

    @Mock
    private RevenueReportMapper revenueReportMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminReportsService = new AdminReportsService(
                attendanceStatRepository,
                eventStatRepository,
                revenueStatRepository,
                eventReportMapper,
                attendanceReportMapper,
                revenueReportMapper
        );
    }

    @Test
    void testGetEventReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 5);
        List<EventStatEntity> mockStats = List.of(
                new EventStatEntity(null, "Yoga", 10, 7, 3),
                new EventStatEntity(null, "Dance", 20, 15, 5)
        );
        Page<EventStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());
        when(eventStatRepository.findAll(pageable)).thenReturn(mockPage);
        when(eventReportMapper.toDto(mockStats.get(0))).thenReturn(new EventReportDto("Yoga", 10, 7, 3));
        when(eventReportMapper.toDto(mockStats.get(1))).thenReturn(new EventReportDto("Dance", 20, 15, 5));

        Page<EventReportDto> result = adminReportsService.getEventReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).eventType());
        assertEquals("Dance", result.getContent().get(1).eventType());
    }

    @Test
    void testGetAttendanceReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 5);
        List<AttendanceStatEntity> mockStats = List.of(
                new AttendanceStatEntity(null, "Yoga", 10, 7, 2, 1),
                new AttendanceStatEntity(null, "Dance", 20, 15, 4, 1)
        );
        Page<AttendanceStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());
        when(attendanceStatRepository.findAll(pageable)).thenReturn(mockPage);
        when(attendanceReportMapper.toDto(mockStats.get(0))).thenReturn(new AttendanceReportDto("Yoga", 10, 7, 2, 1));
        when(attendanceReportMapper.toDto(mockStats.get(1))).thenReturn(new AttendanceReportDto("Dance", 20, 15, 4, 1));

        Page<AttendanceReportDto> result = adminReportsService.getAttendanceReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).eventName());
        assertEquals("Dance", result.getContent().get(1).eventName());
    }

    @Test
    void testGetRevenueReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 5);
        List<RevenueStatEntity> mockStats = List.of(
                new RevenueStatEntity(null, MembershipCardType.MONTHLY_4, new BigDecimal("2000.0"), 10),
                new RevenueStatEntity(null, MembershipCardType.MONTHLY_8, new BigDecimal("5000.0"), 20)
        );
        Page<RevenueStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());
        when(revenueStatRepository.findAll(pageable)).thenReturn(mockPage);
        when(revenueReportMapper.toDto(mockStats.get(0))).thenReturn(new RevenueReportDto(MembershipCardType.MONTHLY_4.getDisplayName(), new BigDecimal("2000.0"), 10));
        when(revenueReportMapper.toDto(mockStats.get(1))).thenReturn(new RevenueReportDto(MembershipCardType.MONTHLY_8.getDisplayName(), new BigDecimal("5000.0"), 20));

        Page<RevenueReportDto> result = adminReportsService.getRevenueReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(MembershipCardType.MONTHLY_4.getDisplayName(), result.getContent().get(0).membershipType());
        assertEquals(MembershipCardType.MONTHLY_8.getDisplayName(), result.getContent().get(1).membershipType());
    }

    @Test
    void testGetEventReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<EventStatEntity> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(eventStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<EventReportDto> result = adminReportsService.getEventReports(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetAttendanceReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<AttendanceStatEntity> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(attendanceStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<AttendanceReportDto> result = adminReportsService.getAttendanceReports(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetRevenueReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<RevenueStatEntity> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(revenueStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<RevenueReportDto> result = adminReportsService.getRevenueReports(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetEventReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(PaginationCodes.PAGE_INDEX_CANNOT_BE_0.getMessage(), exception.getMessage());
    }

    @Test
    void testGetAttendanceReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(PaginationCodes.PAGE_INDEX_CANNOT_BE_0.getMessage(), exception.getMessage());
    }

    @Test
    void testGetRevenueReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(PaginationCodes.PAGE_INDEX_CANNOT_BE_0.getMessage(), exception.getMessage());
    }

    @Test
    void testGetEventReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(PaginationCodes.PAGE_SIZE_LESS_THAN_0.getMessage(), exception.getMessage());
    }

    @Test
    void testGetAttendanceReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(PaginationCodes.PAGE_SIZE_LESS_THAN_0.getMessage(), exception.getMessage());
    }

    @Test
    void testGetRevenueReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(PaginationCodes.PAGE_SIZE_LESS_THAN_0.getMessage(), exception.getMessage());
    }
}