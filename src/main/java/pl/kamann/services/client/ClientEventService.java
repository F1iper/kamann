package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventDto;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventDto> getAvailableEvents(AppUser client) {
        if (client == null) {
            throw new ApiException("Client cannot be null.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_REQUEST);
        }

        return eventRepository.findAvailableEventsExcludingClient(LocalDateTime.now(), client.getId())
                .stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public List<EventDto> getRegisteredEvents(AppUser client) {
        if (client == null) {
            throw new ApiException("Client cannot be null.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_REQUEST);
        }

        return eventRepository.findRegisteredEvents(client)
                .stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public EventDto getEventDetails(Long eventId) {
        var event = findEventById(eventId);
        return eventMapper.toDto(event);
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND));
    }
}
