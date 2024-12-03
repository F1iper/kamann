package pl.kamann.services.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EntityLookupService entityLookupService;

    @InjectMocks
    private AdminEventService adminEventService;

    @Test
    void createEventShouldReturnEventDtoWhenSuccessful() {
        EventDto eventDto = EventDto.builder()
                .title("Test Event")
                .description("Description")
                .instructorId(1L)
                .createdById(2L)
                .eventTypeId(3L)
                .maxParticipants(20)
                .build();

        AppUser createdBy = new AppUser();
        AppUser instructor = new AppUser();
        EventType eventType = new EventType();
        Event event = new Event();
        Event savedEvent = new Event();
        EventDto savedEventDto = EventDto.builder().id(1L).title("Test Event").build();

        when(entityLookupService.findUserById(2L)).thenReturn(createdBy);
        when(entityLookupService.findUserById(1L)).thenReturn(instructor);
        when(entityLookupService.findEventTypeById(3L)).thenReturn(eventType);
        when(eventMapper.toEntity(eventDto)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.toDto(savedEvent)).thenReturn(savedEventDto);

        EventDto result = adminEventService.createEvent(eventDto);

        assertEquals(savedEventDto, result);
        verify(eventRepository).save(event);
        verify(eventMapper).toEntity(eventDto);
        verify(eventMapper).toDto(savedEvent);
    }

    @Test
    void createEventShouldThrowExceptionWhenInstructorNotFound() {
        var eventDto = EventDto.builder()
                .createdById(2L)
                .instructorId(1L)
                .eventTypeId(3L)
                .build();

        var adminUser = new AppUser();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");

        var eventType = new EventType();
        eventType.setId(3L);
        eventType.setName("Yoga");

        when(entityLookupService.findUserById(eq(2L)))
                .thenReturn(adminUser);
        when(entityLookupService.findUserById(eq(1L)))
                .thenThrow(new ApiException("Instructor not found", HttpStatus.NOT_FOUND, Codes.INSTRUCTOR_NOT_FOUND));

        var exception = assertThrows(ApiException.class, () -> adminEventService.createEvent(eventDto));

        assertEquals("Instructor not found", exception.getMessage());
        verify(entityLookupService).findUserById(2L);
        verify(entityLookupService).findUserById(1L);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void updateEventShouldUpdateEventDetails() {
        Long eventId = 1L;
        EventDto eventDto = EventDto.builder()
                .title("Updated Event")
                .instructorId(2L)
                .eventTypeId(3L)
                .build();

        Event existingEvent = new Event();
        AppUser instructor = new AppUser();
        EventType eventType = new EventType();
        Event updatedEvent = new Event();
        EventDto updatedEventDto = EventDto.builder().title("Updated Event").build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(entityLookupService.findUserById(2L)).thenReturn(instructor);
        when(entityLookupService.findEventTypeById(3L)).thenReturn(eventType);
        when(eventRepository.save(existingEvent)).thenReturn(updatedEvent);
        when(eventMapper.toDto(updatedEvent)).thenReturn(updatedEventDto);

        EventDto result = adminEventService.updateEvent(eventId, eventDto);

        assertEquals(updatedEventDto, result);
        verify(eventMapper).updateEventFromDto(existingEvent, eventDto);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void deleteEventShouldDeleteEventWhenNoParticipants() {
        Long eventId = 1L;
        var event = mock(Event.class);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getAttendances()).thenReturn(List.of());

        adminEventService.deleteEvent(eventId, false);

        verify(eventRepository).delete(event);
    }


    @Test
    void deleteEventShouldThrowExceptionWhenParticipantsExist() {
        Long eventId = 1L;
        Event event = mock(Event.class);
        AppUser participant = AppUser.builder()
                .id(2L)
                .email("participant@example.com")
                .build();
        Attendance attendance = Attendance.builder()
                .id(1L)
                .user(participant)
                .event(event)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getAttendances()).thenReturn(List.of(attendance));

        ApiException exception = assertThrows(ApiException.class, () -> adminEventService.deleteEvent(eventId, false));

        assertEquals("Cannot delete event with participants unless forced", exception.getMessage());
        verify(eventRepository, never()).delete(event);
    }

    @Test
    void cancelEventShouldUpdateStatusToCanceledAndNotifyParticipants() {
        Long eventId = 1L;
        Event event = new Event();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        adminEventService.cancelEvent(eventId);

        assertEquals(EventStatus.CANCELED, event.getStatus());
        verify(eventRepository).save(event);
        verify(notificationService).notifyParticipants(event);
    }

    @Test
    void listAllEventsShouldReturnPageOfEventDtos() {
        Pageable pageable = Pageable.unpaged();
        Event event = new Event();
        EventDto eventDto = EventDto.builder().id(1L).title("Event").build();

        when(eventRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        Page<EventDto> result = adminEventService.listAllEvents(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(eventDto, result.getContent().get(0));
        verify(eventRepository).findAll(pageable);
    }

}