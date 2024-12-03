package pl.kamann.services.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventTypeRepository eventTypeRepository;

    @Transactional
    public EventDto createEvent(EventDto eventDto, AppUser createdBy, AppUser instructor, EventType eventType) {
        var event = eventMapper.toEntity(eventDto, createdBy, instructor, eventType);
        var savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto updatedEventDto, AppUser instructor, EventType eventType) {
        var existingEvent = findEventById(eventId);
        eventMapper.updateEventFromDto(existingEvent, updatedEventDto, instructor, eventType);
        var savedEvent = eventRepository.save(existingEvent);
        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        var event = findEventById(eventId);
        if (!event.getAttendances().isEmpty()) {
            throw new ApiException("Cannot delete an event with participants.",
                    HttpStatus.BAD_REQUEST,
                    Codes.EVENT_HAS_PARTICIPANTS);
        }
        eventRepository.delete(event);
    }

    public List<EventDto> listAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND, Codes.EVENT_NOT_FOUND));
    }

    public EventType findEventTypeById(Long eventTypeId) {
        return eventTypeRepository.findById(eventTypeId)
                .orElseThrow(() -> new ApiException(
                        "EventType not found with ID: " + eventTypeId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_TYPE_NOT_FOUND));
    }

    public EventDto getEventById(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND
                ));
        return eventMapper.toDto(event);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND
                ));

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Cannot cancel an event that has already started.",
                    HttpStatus.BAD_REQUEST,
                    Codes.CANNOT_CANCEL_STARTED_EVENT
            );
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }
}
