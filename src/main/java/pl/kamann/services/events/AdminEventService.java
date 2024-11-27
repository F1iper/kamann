package pl.kamann.services.events;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.event.EventDto;


import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventService eventService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        return eventService.createEvent(eventDto);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto eventDto) {
        return eventService.updateEvent(eventId, eventDto);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        eventService.deleteEvent(eventId);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        eventService.cancelEvent(eventId);
    }

    public List<EventDto> listAllEvents() {
        return eventService.listAllEvents();
    }

    public EventDto getEventById(Long eventId) {
        return eventService.getEventById(eventId);
    }
}

