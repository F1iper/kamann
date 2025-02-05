package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public EventDto getEventDetails(Long eventId) {
        return eventMapper.toDto(findEventById(eventId));
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + id,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }
}
