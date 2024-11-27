package pl.kamann.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.event.EventDto;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.mappers.events.EventMapper;
import pl.kamann.services.events.EventService;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EntityLookupService lookupService;

    @InjectMocks
    private EventService eventService;

    private EventType mockEventType;
    private AppUser mockAdmin;
    private AppUser mockInstructor;
    private AppUser mockClient;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockEventType = EventType.builder()
                .id(1L)
                .name("Workshop")
                .description("Technical Workshop")
                .build();

        mockAdmin = AppUser.builder()
                .id(1L)
                .email("admin@example.com")
                .build();

        mockInstructor = AppUser.builder()
                .id(2L)
                .email("instructor@example.com")
                .build();

        mockClient = AppUser.builder()
                .id(3L)
                .email("client@example.com")
                .build();

        Attendance mockAttendance = Attendance.builder()
                .id(1L)
                .user(mockClient)
                .event(mockEvent)
                .status(AttendanceStatus.REGISTERED)
                .timestamp(LocalDateTime.now())
                .build();

        mockEvent = Event.builder()
                .id(1L)
                .title("Spring Boot Workshop")
                .description("Learn Spring Boot")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .maxParticipants(20)
                .status(EventStatus.SCHEDULED)
                .createdBy(mockAdmin)
                .instructor(mockInstructor)
                .eventType(mockEventType)
                .participants(List.of(mockAttendance))
                .build();
    }

    @Test
    void createEventShouldCreateEventWhenEventTypeIdIsValid() {
        EventDto eventDto = EventDto.builder()
                .title("Pole Dance Workshop")
                .description("Learn pole dance")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .recurring(false)
                .maxParticipants(30)
                .eventTypeId(1L)
                .instructorId(2L)
                .build();

        when(lookupService.getLoggedInUser()).thenReturn(mockAdmin);
        when(lookupService.findUserById(2L)).thenReturn(mockInstructor);
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.of(mockEventType));
        when(eventMapper.toEntity(eventDto, mockAdmin, mockInstructor, mockEventType)).thenReturn(mockEvent);
        when(eventRepository.save(mockEvent)).thenReturn(mockEvent);
        when(eventMapper.toDto(mockEvent)).thenReturn(eventDto);

        EventDto result = eventService.createEvent(eventDto);

        assertNotNull(result);
        assertEquals("Pole Dance Workshop", result.getTitle());
        verify(eventRepository, times(1)).save(mockEvent);
    }

    @Test
    void createEventShouldThrowExceptionWhenEventTypeNotFound() {
        EventDto eventDto = EventDto.builder()
                .title("Spring Boot Workshop")
                .eventTypeId(99L)
                .build();

        when(eventTypeRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> eventService.createEvent(eventDto));
        assertEquals(Codes.EVENT_TYPE_NOT_FOUND, exception.getCode());
    }

    @Test
    void getEventByIdShouldReturnEventDto() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(eventMapper.toDto(mockEvent)).thenReturn(EventDto.builder()
                .id(1L)
                .title("Event Title")
                .build());

        EventDto result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Event Title", result.getTitle());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventByIdShouldThrowExceptionWhenEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> eventService.getEventById(99L));
        assertEquals(Codes.EVENT_NOT_FOUND, exception.getCode());
    }

    @Test
    void getUpcomingEventsForLoggedInClientShouldReturnUpcomingEvents() {
        when(lookupService.getLoggedInUser()).thenReturn(mockClient);

        when(eventRepository.findUpcomingEventsForUser(eq(mockClient), any(LocalDateTime.class)))
                .thenReturn(List.of(mockEvent));

        when(eventMapper.toDto(mockEvent)).thenReturn(EventDto.builder()
                .id(mockEvent.getId())
                .title(mockEvent.getTitle())
                .startTime(mockEvent.getStartTime())
                .build());

        List<EventDto> result = eventService.getUpcomingEventsForLoggedInClient();

        assertEquals(1, result.size());
        assertEquals("Spring Boot Workshop", result.get(0).getTitle());
        verify(eventRepository, times(1)).findUpcomingEventsForUser(eq(mockClient), any(LocalDateTime.class));
    }

    @Test
    void deleteEventShouldDeleteEventSuccessfully() {
        when(lookupService.findEventById(1L)).thenReturn(mockEvent);

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).delete(mockEvent);
    }

    @Test
    void updateEventShouldUpdateEventSuccessfully() {
        EventDto updatedEventDto = EventDto.builder()
                .title("Updated Event Title")
                .description("Updated Description")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .recurring(true)
                .maxParticipants(50)
                .instructorId(2L)
                .eventTypeId(1L)
                .build();

        when(lookupService.findEventById(1L)).thenReturn(mockEvent);
        when(lookupService.findUserById(2L)).thenReturn(mockInstructor);
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.of(mockEventType));
        when(eventRepository.save(mockEvent)).thenReturn(mockEvent);
        when(eventMapper.toDto(mockEvent)).thenReturn(updatedEventDto);

        EventDto result = eventService.updateEvent(1L, updatedEventDto);

        assertEquals("Updated Event Title", result.getTitle());
        verify(eventRepository, times(1)).save(mockEvent);
    }

    @Test
    void cancelEventShouldCancelEventSuccessfully() {
        when(lookupService.findEventById(1L)).thenReturn(mockEvent);

        eventService.cancelEvent(1L);

        assertEquals(EventStatus.CANCELLED, mockEvent.getStatus());
        verify(eventRepository, times(1)).save(mockEvent);
    }

    @Test
    void cancelEventShouldThrowExceptionWhenEventHasStarted() {
        mockEvent.setStartTime(LocalDateTime.now().minusHours(1)); // Past start time
        when(lookupService.findEventById(1L)).thenReturn(mockEvent);

        ApiException exception = assertThrows(ApiException.class, () -> eventService.cancelEvent(1L));
        assertEquals(Codes.CANNOT_CANCEL_STARTED_EVENT, exception.getCode());
    }
}
