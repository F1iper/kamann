package pl.kamann.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EntityLookupService lookupService;

    public EventDto createEvent(EventDto eventDto) {
        AppUser createdBy = lookupService.findUserById(eventDto.getCreatedById());
        AppUser instructor = lookupService.findUserById(eventDto.getInstructorId());
        EventType eventType = lookupService.findEventTypeById(eventDto.getEventTypeId());
        Event event = eventMapper.toEntity(eventDto, createdBy, instructor, eventType);
        return eventMapper.toDto(eventRepository.save(event), List.of());
    }

    public EventDto updateEvent(Long eventId, EventDto updatedEventDto) {
        Event existingEvent = lookupService.findEventById(eventId);
        AppUser instructor = lookupService.findUserById(updatedEventDto.getInstructorId());
        EventType eventType = lookupService.findEventTypeById(updatedEventDto.getEventTypeId());
        eventMapper.updateEventFromDto(existingEvent, updatedEventDto, instructor, eventType);
        Event updatedEvent = eventRepository.save(existingEvent);
        return eventMapper.toDto(updatedEvent, List.of());
    }

    public void deleteEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        eventRepository.delete(event);
    }

    public void cancelEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException("Cannot cancel an event that has already started.", HttpStatus.BAD_REQUEST, Codes.CANNOT_CANCEL_STARTED_EVENT);
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    public EventDto getEventById(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        return eventMapper.toDto(event, List.of());
    }

    public Page<EventDto> searchEvents(LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        return eventRepository.findFilteredEvents(startDate, endDate, null, null, keyword, pageable)
                .map(event -> eventMapper.toDto(event, List.of()));
    }

    public List<EventDto> getUpcomingEvents(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        List<Event> events = eventRepository.findUpcomingEventsForUser(user, LocalDateTime.now());
        return events.stream()
                .map(event -> eventMapper.toDto(event, List.of()))
                .collect(Collectors.toList());
    }
}
