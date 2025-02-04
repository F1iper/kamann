package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(eventMapper::toDto).toList();
    }

    public EventDto getEventDetails(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
        return eventMapper.toDto(event);
    }

    public EventDto createEvent(EventDto dto) {
        Event event = eventMapper.toEntity(dto);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
    }

    public EventDto updateEvent(Long id, EventDto dto) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + id,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        // Update fields
        existingEvent.setTitle(dto.title());
        existingEvent.setDescription(dto.description());
        existingEvent.setRecurring(dto.recurring());
        existingEvent.setRrule(dto.rrule());
        existingEvent.setStartDate(dto.startDate());
        existingEvent.setStartTime(dto.startTime());
        existingEvent.setEndTime(dto.endTime());

        // Save updated entity
        Event updatedEvent = eventRepository.save(existingEvent);

        return eventMapper.toDto(updatedEvent);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ApiException(
                    "No event found with ID: " + id,
                    HttpStatus.NOT_FOUND,
                    EventCodes.EVENT_NOT_FOUND.name()
            );
        }

        // Delete the parent event (and cascade delete its occurrences)
        eventRepository.deleteById(id);
    }
}
