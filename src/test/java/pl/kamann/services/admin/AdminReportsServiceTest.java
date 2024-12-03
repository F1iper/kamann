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
import pl.kamann.dtos.EventReportDto;
import pl.kamann.dtos.EventStat;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.repositories.EventRepository;

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
    private EventReportMapper eventReportMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminReportsService = new AdminReportsService(eventRepository, eventReportMapper);
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
}
