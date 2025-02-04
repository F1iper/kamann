package pl.kamann.services.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private OccurrenceEventRepository occurrenceEventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private PaginationService paginationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EntityLookupService entityLookupService;

    @InjectMocks
    private AdminEventService adminEventService;

    @Test
    void listAllEventsShouldReturnPaginatedEventDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Pageable validatedPageable = PageRequest.of(0, 10);
        Event event = new Event();
        EventDto eventDto = EventDto.builder().id(1L).title("Event Title").build();

        when(paginationService.validatePageable(pageable)).thenReturn(validatedPageable);
        when(eventRepository.findAll(validatedPageable)).thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        PaginatedResponseDto<EventDto> result = adminEventService.listAllEvents(pageable);

        assertEquals(1, result.getMetaData().getTotalElements());
        assertEquals(eventDto, result.getContent().get(0));
        verify(paginationService).validatePageable(pageable);
        verify(eventRepository).findAll(validatedPageable);
        verify(eventMapper).toDto(event);
    }
}
