package pl.kamann.services.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.UserEventHistoryRepository;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class ClientEventHistoryServiceTest {

    @InjectMocks
    private ClientEventHistoryService clientEventHistoryService;

    @Mock
    private UserEventHistoryRepository userEventHistoryRepository;

    @Mock
    private ClientEventService clientEventService;

    @Mock
    private EventMapper eventMapper;

    private AppUser mockUser;
    private Event mockEvent;
    private EventDto mockEventDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new AppUser();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockEvent = new Event();
        mockEvent.setId(1L);
        mockEvent.setTitle("Dance Event");
    }

    @Test
    void testUpdateEventHistory() {
        EventDto eventDto = EventDto.builder()
                .id(1L)
                .title("Dance Event")
                .description("A fun dance event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .recurring(false)
                .createdById(1L)
                .instructorId(2L)
                .maxParticipants(30)
                .status(EventStatus.SCHEDULED)
                .currentParticipants(10)
                .eventTypeId(1L)
                .eventTypeName("Salsa")
                .build();

        when(clientEventService.getEventDetails(1L)).thenReturn(eventDto);
        when(eventMapper.toEntity(eventDto)).thenReturn(mockEvent);

        clientEventHistoryService.updateEventHistory(mockUser, 1L, AttendanceStatus.PRESENT);

        verify(clientEventService, times(1)).getEventDetails(1L);
        verify(eventMapper, times(1)).toEntity(eventDto);
        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }

    @Test
    void testLogEventHistoryPresent() {
        clientEventHistoryService.logEventHistory(mockUser, mockEvent, AttendanceStatus.PRESENT);

        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }

    @Test
    void testLogEventHistoryAbsent() {
        clientEventHistoryService.logEventHistory(mockUser, mockEvent, AttendanceStatus.ABSENT);

        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }

}
