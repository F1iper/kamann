package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.EventDto;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final EventRepository eventRepository;
    private final EntityLookupService lookupService;
    private final EventMapper eventMapper;

    /**
     * Get a list of upcoming events that the client can join.
     *
     * @return List of upcoming events
     */
    public List<EventDto> getAvailableEvents() {
        AppUser client = lookupService.getLoggedInUser();

        // Fetch available events and filter those not registered
        return eventRepository.findAvailableEvents(LocalDateTime.now()).stream()
                .filter(event -> event.getParticipants().stream().noneMatch(p -> p.equals(client)))
                .map(eventMapper::toDto)
                .toList();
    }

    /**
     * Get a list of events the client is registered for.
     *
     * @return List of registered events
     */
    public List<EventDto> getRegisteredEvents() {
        AppUser client = lookupService.getLoggedInUser();

        return eventRepository.findRegisteredEvents(client).stream()
                .map(eventMapper::toDto)
                .toList();
    }

    /**
     * View details of an event by ID.
     *
     * @param eventId Event ID
     * @return Event details
     */
    public EventDto getEventDetails(Long eventId) {
        return eventRepository.findById(eventId)
                .map(eventMapper::toDto)
                .orElseThrow(() -> new ApiException("Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND, Codes.EVENT_NOT_FOUND));
    }
}
