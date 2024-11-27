package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.EventDto;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventStatus;
import pl.kamann.entities.EventType;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;
import pl.kamann.mappers.EventMapper;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventTypeRepository eventTypeRepository;
    private final EntityLookupService lookupService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        AppUser createdBy = lookupService.getLoggedInUser();
        AppUser instructor = lookupService.findUserById(eventDto.getInstructorId());
        EventType eventType = findEventTypeById(eventDto.getEventTypeId());

        Event event = eventMapper.toEntity(eventDto, createdBy, instructor, eventType);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto updatedEventDto) {
        Event existingEvent = lookupService.findEventById(eventId);
        AppUser instructor = lookupService.findUserById(updatedEventDto.getInstructorId());
        EventType eventType = findEventTypeById(updatedEventDto.getEventTypeId());

        eventMapper.updateEventFromDto(existingEvent, updatedEventDto, instructor, eventType);
        return eventMapper.toDto(eventRepository.save(existingEvent));
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        eventRepository.delete(event);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException("Cannot cancel an event that has already started.",
                    HttpStatus.BAD_REQUEST, Codes.CANNOT_CANCEL_STARTED_EVENT);
        }
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    public List<EventDto> listAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public EventType findEventTypeById(Long eventTypeId) {
        return eventTypeRepository.findById(eventTypeId)
                .orElseThrow(() -> new ApiException("EventType not found with ID: " + eventTypeId,
                        HttpStatus.NOT_FOUND, Codes.EVENT_TYPE_NOT_FOUND));
    }

    public List<EventDto> getUpcomingEventsForLoggedInClient() {
        AppUser loggedInUser = lookupService.getLoggedInUser();

        List<Event> upcomingEvents = eventRepository.findUpcomingEventsForUser(loggedInUser, LocalDateTime.now());

        return upcomingEvents.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND));

        return eventMapper.toDto(event);
    }

}
