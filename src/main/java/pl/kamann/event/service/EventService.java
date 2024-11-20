package pl.kamann.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.specific.*;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventMapper eventMapper;

    public EventDto createEvent(EventDto eventDto) {
        AppUser instructor = appUserRepository.findById(eventDto.getInstructorId())
                .orElseThrow(() -> new InstructorNotFoundException("Instructor not found"));

        AppUser createdBy = appUserRepository.findById(eventDto.getCreatedById())
                .orElseThrow(() -> new UsernameNotFoundException("Created by user not found"));

        if (isInstructorBusy(instructor, eventDto.getStartTime(), eventDto.getEndTime())) {
            throw new InstructorBusyException("Instructor is already booked during this time");
        }

        EventType eventType = eventTypeRepository.findByName(eventDto.getEventTypeName())
                .orElseThrow(() -> new EventTypeNotFoundException("Event type not found"));

        if (eventDto.getEndTime().isBefore(eventDto.getStartTime())) {
            throw new InvalidEventTimeException("End time must be after start time");
        }

        eventDto.setStatus(EventStatus.UPCOMING);

        Event event = eventMapper.toEntity(eventDto, createdBy, instructor, eventType);
        return eventMapper.toDTO(eventRepository.save(event));
    }

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        return eventMapper.toDTO(event);
    }

    public EventDto updateEvent(Long id, EventDto updatedEventDto) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));

        if (!existingEvent.getStartTime().equals(updatedEventDto.getStartTime()) ||
                !existingEvent.getEndTime().equals(updatedEventDto.getEndTime())) {
            AppUser instructor = appUserRepository.findById(updatedEventDto.getInstructorId())
                    .orElseThrow(() -> new InstructorNotFoundException("Instructor not found"));

            if (isInstructorBusy(instructor,
                    updatedEventDto.getStartTime(),
                    updatedEventDto.getEndTime())) {
                throw new InstructorBusyException("Instructor is already booked during this time");
            }
        }

        if (updatedEventDto.getEndTime().isBefore(updatedEventDto.getStartTime())) {
            throw new InvalidEventTimeException("End time must be after start time");
        }

        EventType eventType = existingEvent.getEventType();
        if (updatedEventDto.getEventTypeName() != null) {
            eventType = eventTypeRepository.findByName(updatedEventDto.getEventTypeName())
                    .orElseThrow(() -> new EventTypeNotFoundException("Event type not found"));
        }

        AppUser instructor = existingEvent.getInstructor();
        if (updatedEventDto.getInstructorId() != null) {
            instructor = appUserRepository.findById(updatedEventDto.getInstructorId())
                    .orElseThrow(() -> new InstructorNotFoundException("Instructor not found"));
        }

        existingEvent.setTitle(updatedEventDto.getTitle());
        existingEvent.setDescription(updatedEventDto.getDescription());
        existingEvent.setStartTime(updatedEventDto.getStartTime());
        existingEvent.setEndTime(updatedEventDto.getEndTime());
        existingEvent.setRecurring(updatedEventDto.isRecurring());
        existingEvent.setMaxParticipants(updatedEventDto.getMaxParticipants());
        existingEvent.setInstructor(instructor);
        existingEvent.setEventType(eventType);

        return eventMapper.toDTO(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        eventRepository.delete(event);
    }

    public boolean isEventFull(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
        int currentParticipants = attendanceRepository.countByEventAndStatus(
                event, AttendanceStatus.PRESENT
        );
        return currentParticipants >= event.getMaxParticipants();
    }

    private boolean isInstructorBusy(AppUser instructor, LocalDateTime start, LocalDateTime end) {
        List<Event> overlappingEvents = eventRepository.findByInstructorAndStartTimeBetween(
                instructor,
                start.minusHours(2),
                end.plusHours(2)
        );

        return overlappingEvents.stream().anyMatch(existingEvent ->
                !(end.isBefore(existingEvent.getStartTime()) ||
                        start.isAfter(existingEvent.getEndTime()))
        );
    }

    public List<EventDto> getEventsByInstructor(Long instructorId) {
        AppUser instructor = appUserRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Event> events = eventRepository.findByInstructor(instructor);
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }
}