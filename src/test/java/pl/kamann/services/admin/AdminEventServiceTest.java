package pl.kamann.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.kamann.dtos.EventResponseDto;
import pl.kamann.dtos.EventUpdateRequestDto;
import pl.kamann.entities.event.EventUpdateScope;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.NotificationService;
import pl.kamann.services.admin.AdminEventService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminEventServiceTest {

    private AdminEventService adminEventService;
    private EventValidationService eventValidationService;
    private NotificationService notificationService;
    private EntityLookupService entityLookupService;
    private PaginationService paginationService;
    private EventMapper eventMapper;
    private EventRepository eventRepository;
    private OccurrenceEventRepository occurrenceEventRepository;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        occurrenceEventRepository = mock(OccurrenceEventRepository.class);
        adminEventService = new AdminEventService(eventRepository,
                occurrenceEventRepository,
                eventMapper,
                eventValidationService,
                notificationService,
                entityLookupService,
                paginationService);
    }

    @Test
    void updateEvent_shouldPropagateChangesForAllOccurrences() {
        AppUser instructor = AppUser.builder().id(2L).firstName("Jane").lastName("Smith").build();
        EventType type = EventType.builder().id(1L).name("Yoga").build();
        Event event = Event.builder()
                .id(100L)
                .title("Morning Yoga")
                .description("Old description")
                .start(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .rrule("FREQ=DAILY")
                .status(EventStatus.SCHEDULED)
                .eventType(type)
                .maxParticipants(15)
                .instructor(instructor)
                .build();
        OccurrenceEvent occ = OccurrenceEvent.builder()
                .id(200L)
                .event(event)
                .start(event.getStart().plusDays(2))
                .durationMinutes(60)
                .instructor(instructor)
                .build();
        event.setOccurrences(Collections.singletonList(occ));

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(occurrenceEventRepository.findAllByEventId(100L)).thenReturn(Collections.singletonList(occ));

        EventUpdateRequestDto requestDto = EventUpdateRequestDto.builder()
                .id(100L)
                .title("Morning Yoga Updated")
                .description("New description")
                .start(event.getStart())
                .durationMinutes(90)
                .rrule("FREQ=DAILY")
                .instructorId(2L)
                .maxParticipants(20)
                .build();

        EventResponseDto responseDto = adminEventService.updateEvent(100L, requestDto, EventUpdateScope.ALL_OCCURRENCES);

        assertEquals("Morning Yoga Updated", responseDto.title());
        assertEquals(90, responseDto.durationMinutes());
        assertEquals(20, responseDto.maxParticipants());

        ArgumentCaptor<OccurrenceEvent> captor = ArgumentCaptor.forClass(OccurrenceEvent.class);
        verify(occurrenceEventRepository, times(1)).save(captor.capture());
        OccurrenceEvent updatedOcc = captor.getValue();
        assertEquals(90, updatedOcc.getDurationMinutes());
        assertEquals(20, updatedOcc.getMaxParticipants());
        assertEquals(instructor, updatedOcc.getInstructor());
    }

}
