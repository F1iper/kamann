package pl.kamann.services.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.reports.AttendanceStatEntity;
import pl.kamann.entities.reports.EventStatEntity;
import pl.kamann.entities.reports.RevenueStatEntity;
import pl.kamann.repositories.admin.AttendanceStatRepository;
import pl.kamann.repositories.admin.EventStatRepository;
import pl.kamann.repositories.admin.RevenueStatRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AdminReportsServiceTest {

    private AdminReportsService adminReportsService;

    @Mock
    private EventStatRepository eventStatRepository;

    @Mock
    private AttendanceStatRepository attendanceStatRepository;

    @Mock
    private RevenueStatRepository revenueStatRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminReportsService = new AdminReportsService(
                attendanceStatRepository,
                eventStatRepository,
                revenueStatRepository
        );
    }

    @Test
    void testGetEventReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<EventStatEntity> mockStats = List.of(
                new EventStatEntity(null, "Yoga", 10, 7, 3),
                new EventStatEntity(null, "Dance", 20, 15, 5)
        );
        Page<EventStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(eventStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<EventStatEntity> result = adminReportsService.getEventReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).getEventType());
        assertEquals(10, result.getContent().get(0).getTotalEvents());
    }

    @Test
    void testGetEventReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<EventStatEntity> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(eventStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<EventStatEntity> result = adminReportsService.getEventReports(pageable);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetAttendanceReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<AttendanceStatEntity> mockStats = List.of(
                new AttendanceStatEntity(null, "Yoga", 10, 7, 2, 1),
                new AttendanceStatEntity(null, "Dance", 20, 15, 4, 1)
        );
        Page<AttendanceStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(attendanceStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<AttendanceStatEntity> result = adminReportsService.getAttendanceReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Yoga", result.getContent().get(0).getEventName());
        assertEquals(10, result.getContent().get(0).getTotalParticipants());
    }

    @Test
    void testGetAttendanceReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<AttendanceStatEntity> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(attendanceStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<AttendanceStatEntity> result = adminReportsService.getAttendanceReports(pageable);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetRevenueReportsNormalCase() {
        Pageable pageable = PageRequest.of(0, 2);
        List<RevenueStatEntity> mockStats = List.of(
                new RevenueStatEntity(null, "Basic", 2000.0, 10),
                new RevenueStatEntity(null, "Premium", 5000.0, 20)
        );
        Page<RevenueStatEntity> mockPage = new PageImpl<>(mockStats, pageable, mockStats.size());

        when(revenueStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<RevenueStatEntity> result = adminReportsService.getRevenueReports(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Basic", result.getContent().get(0).getMembershipType());
        assertEquals(2000.0, result.getContent().get(0).getTotalRevenue());
    }

    @Test
    void testGetRevenueReportsEmptyDataset() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<RevenueStatEntity> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(revenueStatRepository.findAll(pageable)).thenReturn(mockPage);

        Page<RevenueStatEntity> result = adminReportsService.getRevenueReports(pageable);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetEventReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(Codes.PAGE_INDEX_CANNOT_BE_0, exception.getMessage());
    }

    @Test
    void testGetAttendanceReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(Codes.PAGE_INDEX_CANNOT_BE_0, exception.getMessage());
    }

    @Test
    void testGetRevenueReportsInvalidPageNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 2));
        assertEquals(Codes.PAGE_INDEX_CANNOT_BE_0, exception.getMessage());
    }

    @Test
    void testGetEventReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(Codes.PAGE_SIZE_LESS_THAN_0, exception.getMessage());
    }

    @Test
    void testGetAttendanceReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(Codes.PAGE_SIZE_LESS_THAN_0, exception.getMessage());
    }

    @Test
    void testGetRevenueReportsInvalidPageSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, -1));
        assertEquals(Codes.PAGE_SIZE_LESS_THAN_0, exception.getMessage());
    }
}