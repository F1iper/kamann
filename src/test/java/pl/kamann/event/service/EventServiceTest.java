package pl.kamann.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.specific.EventNotFoundException;
import pl.kamann.config.exception.specific.InstructorBusyException;
import pl.kamann.config.exception.specific.InstructorNotFoundException;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private EventService eventService;

    private AppUser instructor;
    private EventType eventType;
    private Event event;

    @BeforeEach
    void setUp() {
        // Create a sample instructor
        instructor = new AppUser();
        instructor.setId(1L);
        instructor.setEmail("instructor@example.com");

        // Create a sample event type
        eventType = new EventType();
        eventType.setId(1L);
        eventType.setName("Pole Dance");

        // Create a sample event
        event = new Event();
        event.setId(1L);
        event.setTitle("Beginner Pole Dance");
        event.setInstructor(instructor);
        event.setEventType(eventType);
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        event.setMaxParticipants(10);
        event.setRecurring(false);
        event.setStatus(EventStatus.UPCOMING);
    }

    @Test
    void createEventSuccess() {
        // given
        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));

        when(eventTypeRepository.findByName(eventType.getName()))
                .thenReturn(Optional.of(eventType));

        when(eventRepository.findByInstructorAndStartTimeBetween(
                any(), any(), any()))
                .thenReturn(List.of());

        when(eventRepository.save(any(Event.class)))
                .thenReturn(event);

        // when
        Event createdEvent = eventService.createEvent(event);

        // then
        assertNotNull(createdEvent);
        assertEquals(EventStatus.UPCOMING, createdEvent.getStatus());
        verify(eventRepository).save(event);
    }

    @Test
    void createEventInstructorNotFound() {
        // given
        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(InstructorNotFoundException.class,
                () -> eventService.createEvent(event));
    }

    @Test
    void createEventInstructorBusy() {
        // given
        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));

        Event existingEvent = new Event();
        existingEvent.setStartTime(event.getStartTime().plusMinutes(30));
        existingEvent.setEndTime(event.getStartTime().plusHours(1));

        when(eventRepository.findByInstructorAndStartTimeBetween(
                any(), any(), any()))
                .thenReturn(Arrays.asList(existingEvent));

        // when & then
        assertThrows(InstructorBusyException.class,
                () -> eventService.createEvent(event));
    }

    @Test
    void updateEventSuccess() {
        // given
        Event existingEvent = new Event();
        existingEvent.setId(1L);
        existingEvent.setInstructor(instructor);
        existingEvent.setStartTime(LocalDateTime.now().plusDays(1));
        existingEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existingEvent));

        when(eventTypeRepository.findByName(eventType.getName()))
                .thenReturn(Optional.of(eventType));

        when(eventRepository.findByInstructorAndStartTimeBetween(
                any(), any(), any()))
                .thenReturn(List.of());

        when(eventRepository.save(any(Event.class)))
                .thenReturn(event);

        // when
        Event updatedEvent = eventService.updateEvent(1L, event);

        // then
        assertNotNull(updatedEvent);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void isEventFullTrue() {
        // given
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT))
                .thenReturn(10);  // Matches max participants

        // when
        boolean isFull = eventService.isEventFull(1L);

        // then
        assertTrue(isFull);
    }

    @Test
    void isEventFullFalse() {
        // given
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT))
                .thenReturn(5);  // Less than max participants

        // when
        boolean isFull = eventService.isEventFull(1L);

        // then
        assertFalse(isFull);
    }

    @Test
    void deleteEventSuccess() {
        // given
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        // when
        eventService.deleteEvent(1L);

        // then
        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEventNotFound() {
        // given
        when(eventRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(EventNotFoundException.class,
                () -> eventService.deleteEvent(1L));
    }
}