package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
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
        validateClient(client);

        return eventRepository.findAvailableEventsExcludingClient(LocalDateTime.now(), client.getId())
                .stream()
                .map(eventMapper::toDto)
                .toList();
    }


    public List<EventDto> getRegisteredEvents(AppUser user) {
        if (user == null) {
            throw new ApiException(
                    "User cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_REQUEST.name()
            );
        }

        var registeredEvents = eventRepository.findEventsByUserAndStatus(user, AttendanceStatus.REGISTERED);
        return registeredEvents.stream()
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
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }

    private void validateClient(AppUser client) {
        if (client == null) {
            throw new ApiException("Client cannot be null.",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_REQUEST.name()
            );
        }
    }
}
