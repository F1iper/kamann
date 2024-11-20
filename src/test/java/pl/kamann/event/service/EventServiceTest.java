package pl.kamann.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.specific.EventNotFoundException;
import pl.kamann.config.exception.specific.InstructorBusyException;
import pl.kamann.config.exception.specific.InstructorNotFoundException;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private AppUser instructor;
    private EventType eventType;
    private Event event;
    private EventDto eventDto;

    @BeforeEach
    void setUp() {
        instructor = new AppUser();
        instructor.setId(1L);
        instructor.setEmail("instructor@example.com");

        eventType = new EventType();
        eventType.setId(1L);
        eventType.setName("Pole Dance");

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

        eventDto = EventDto.builder()
                .id(1L)
                .title("Beginner Pole Dance")
                .instructorId(1L)
                .eventTypeName("Pole Dance")
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxParticipants(10)
                .recurring(false)
                .status(EventStatus.UPCOMING)
                .build();
    }

    @Test
    void createEventSuccess() {
        AppUser createdBy = new AppUser();
        createdBy.setId(2L);
        createdBy.setEmail("createdby@example.com");

        eventDto.setCreatedById(createdBy.getId());

        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));

        when(appUserRepository.findById(createdBy.getId()))
                .thenReturn(Optional.of(createdBy));

        when(eventTypeRepository.findByName(eventType.getName()))
                .thenReturn(Optional.of(eventType));

        when(eventRepository.findByInstructorAndStartTimeBetween(
                eq(instructor), any(), any()))
                .thenReturn(List.of());

        when(eventMapper.toEntity(eventDto, createdBy, instructor, eventType))
                .thenReturn(event);

        when(eventRepository.save(any(Event.class)))
                .thenReturn(event);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        EventDto createdEvent = eventService.createEvent(eventDto);

        assertNotNull(createdEvent);
        assertEquals(EventStatus.UPCOMING, createdEvent.getStatus());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventInstructorNotFound() {
        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.empty());

        assertThrows(InstructorNotFoundException.class,
                () -> eventService.createEvent(eventDto));
    }

    @Test
    void createEventInstructorBusy() {
        AppUser createdBy = new AppUser();
        createdBy.setId(2L);
        createdBy.setEmail("createdby@example.com");
        eventDto.setCreatedById(createdBy.getId());

        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));
        when(appUserRepository.findById(createdBy.getId()))
                .thenReturn(Optional.of(createdBy));

        Event existingEvent = new Event();
        existingEvent.setStartTime(event.getStartTime().plusMinutes(30));
        existingEvent.setEndTime(event.getStartTime().plusHours(1));
        when(eventRepository.findByInstructorAndStartTimeBetween(
                eq(instructor), any(), any()))
                .thenReturn(List.of(existingEvent));

        assertThrows(InstructorBusyException.class,
                () -> eventService.createEvent(eventDto));

        verify(appUserRepository, times(1)).findById(instructor.getId());
        verify(appUserRepository, times(1)).findById(createdBy.getId());
        verify(eventRepository, times(1)).findByInstructorAndStartTimeBetween(
                eq(instructor), any(), any());
    }

    @Test
    void updateEventSuccess() {
        Event existingEvent = new Event();
        existingEvent.setId(1L);
        existingEvent.setInstructor(instructor);
        existingEvent.setStartTime(LocalDateTime.now().plusDays(1));
        existingEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        EventDto updatedDto = new EventDto();
        updatedDto.setTitle("Updated Event");
        updatedDto.setDescription("Updated Description");
        updatedDto.setStartTime(LocalDateTime.now().plusDays(2));
        updatedDto.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        updatedDto.setInstructorId(instructor.getId());
        updatedDto.setEventTypeName(eventType.getName());
        updatedDto.setMaxParticipants(50);
        updatedDto.setRecurring(false);

        when(appUserRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existingEvent));

        when(eventTypeRepository.findByName(eventType.getName()))
                .thenReturn(Optional.of(eventType));

        when(eventRepository.findByInstructorAndStartTimeBetween(
                eq(instructor), any(), any()))
                .thenReturn(List.of());

        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        when(eventMapper.toDto(existingEvent)).thenReturn(updatedDto);

        EventDto result = eventService.updateEvent(1L, updatedDto);

        assertNotNull(result);
        assertEquals("Updated Event", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(updatedDto.getStartTime(), result.getStartTime());
        assertEquals(updatedDto.getEndTime(), result.getEndTime());
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void isEventFullTrue() {

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT))
                .thenReturn(10);

        boolean isFull = eventService.isEventFull(1L);

        assertTrue(isFull);
    }

    @Test
    void isEventFullFalse() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT))
                .thenReturn(5);

        boolean isFull = eventService.isEventFull(1L);

        assertFalse(isFull);
    }

    @Test
    void deleteEventSuccess() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        eventService.deleteEvent(1L);

        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEventNotFound() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> eventService.deleteEvent(1L));
    }

    @Test
    void shouldFilterEvents() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long instructorId = 1L;
        String eventType = "Workshop";
        String keyword = "Yoga";
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findFilteredEvents(eq(startDate), eq(endDate), eq(instructorId), eq(eventType), eq(keyword), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(new Event())));

        Page<EventDto> result = eventService.searchEvents(startDate, endDate, instructorId, eventType, keyword, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(eventRepository, times(1)).findFilteredEvents(eq(startDate), eq(endDate), eq(instructorId), eq(eventType), eq(keyword), eq(pageable));
    }
}