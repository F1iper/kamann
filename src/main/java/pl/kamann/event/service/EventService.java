package pl.kamann.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.config.exception.specific.*;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;

    public Event createEvent(Event event) {
        // Validate instructor
        AppUser instructor = appUserRepository.findById(event.getInstructor().getId())
                .orElseThrow(() -> new InstructorNotFoundException("Instructor not found"));

        // Check if instructor is available
        if (isInstructorBusy(instructor, event.getStartTime(), event.getEndTime())) {
            throw new InstructorBusyException("Instructor is already booked during this time");
        }

        // Validate event type
        EventType eventType = eventTypeRepository.findByName(event.getEventType().getName())
                .orElseThrow(() -> new EventTypeNotFoundException("Event type not found"));
        event.setEventType(eventType);

        // Validate time
        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new InvalidEventTimeException("End time must be after start time");
        }

        event.setStatus(EventStatus.UPCOMING);

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        Event existingEvent = getEventById(id);

        // Validate instructor availability if time changed
        if (!existingEvent.getStartTime().equals(updatedEvent.getStartTime()) ||
                !existingEvent.getEndTime().equals(updatedEvent.getEndTime())) {
            if (isInstructorBusy(updatedEvent.getInstructor(),
                    updatedEvent.getStartTime(),
                    updatedEvent.getEndTime())) {
                throw new InstructorBusyException("Instructor is already booked during this time");
            }
        }

        // Validate end time
        if (updatedEvent.getEndTime().isBefore(updatedEvent.getStartTime())) {
            throw new InvalidEventTimeException("End time must be after start time");
        }

        // Update fields
        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setRecurring(updatedEvent.isRecurring());
        existingEvent.setMaxParticipants(updatedEvent.getMaxParticipants());

        // Only update instructor if provided
        if (updatedEvent.getInstructor() != null) {
            existingEvent.setInstructor(updatedEvent.getInstructor());
        }

        // Only update event type if provided
        if (updatedEvent.getEventType() != null) {
            EventType eventType = eventTypeRepository.findByName(updatedEvent.getEventType().getName())
                    .orElseThrow(() -> new EventTypeNotFoundException("Event type not found"));
            existingEvent.setEventType(eventType);
        }

        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long id) {
        Event event = getEventById(id); // This ensures the event exists
        eventRepository.delete(event);
    }

    public boolean isEventFull(Long eventId) {
        Event event = getEventById(eventId);
        int currentParticipants = attendanceRepository.countByEventAndStatus(
                event, AttendanceStatus.PRESENT
        );
        return currentParticipants >= event.getMaxParticipants();
    }

    private boolean isInstructorBusy(AppUser instructor, LocalDateTime start, LocalDateTime end) {
        List<Event> overlappingEvents = eventRepository.findByInstructorAndStartTimeBetween(
                instructor,
                start.minusHours(2),  // Consider buffer time before and after
                end.plusHours(2)
        );

        return overlappingEvents.stream().anyMatch(existingEvent ->
                !(end.isBefore(existingEvent.getStartTime()) ||
                        start.isAfter(existingEvent.getEndTime()))
        );
    }
}