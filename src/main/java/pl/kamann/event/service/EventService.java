package pl.kamann.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.specific.EventNotFoundException;
import pl.kamann.config.exception.specific.InstructorNotFoundException;
import pl.kamann.event.model.Event;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    public Event createEvent(Event event) {
        AppUser instructor = appUserRepository.findById(event.getInstructor().getId())
                .orElseThrow(() -> new InstructorNotFoundException("Instructor not found"));
        event.setInstructor(instructor);

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setStartTime(updatedEvent.getStartTime());
        event.setEndTime(updatedEvent.getEndTime());
        event.setRecurring(updatedEvent.isRecurring());
        event.setEventType(updatedEvent.getEventType());
        event.setInstructor(updatedEvent.getInstructor());
        event.setMaxParticipants(updatedEvent.getMaxParticipants());

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    // Additional method to check participant capacity
    public boolean isEventFull(Long eventId) {
        Event event = getEventById(eventId);
        // Here you'd want to track current participants, possibly in a `Participant` table
        return event.getMaxParticipants() <= getCurrentParticipants(eventId);
    }

    private int getCurrentParticipants(Long eventId) {
        // Logic to get the current number of participants, e.g., from a join table
        return 0; // Placeholder value
    }
}