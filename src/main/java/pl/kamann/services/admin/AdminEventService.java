package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;


    public EventDto createEvent(EventDto eventDto) {
        if (eventDto.createdById() == null) {
            throw new ApiException(
                    "Created by ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        if (eventDto.instructorId() == null) {
            throw new ApiException(
                    "Instructor ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        var createdBy = entityLookupService.findUserById(eventDto.createdById());
        var instructor = entityLookupService.findUserById(eventDto.instructorId());
        var eventType = entityLookupService.findEventTypeById(eventDto.eventTypeId());

        var event = eventMapper.toEntity(eventDto);
        event.setCreatedBy(createdBy);
        event.setInstructor(instructor);
        event.setEventType(eventType);

        return eventMapper.toDto(eventRepository.save(event));
    }

    public EventDto updateEvent(Long eventId, EventDto eventDto) {
        var existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        var instructor = entityLookupService.findUserById(eventDto.instructorId());
        var eventType = entityLookupService.findEventTypeById(eventDto.eventTypeId());

        eventMapper.updateEventFromDto(existingEvent, eventDto);
        existingEvent.setInstructor(instructor);
        existingEvent.setEventType(eventType);

        return eventMapper.toDto(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id, boolean force) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        if (!force && !event.getAttendances().isEmpty()) {
            throw new ApiException(
                    "Cannot delete event with participants unless forced",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.EVENT_HAS_PARTICIPANTS.name()
            );
        }

        eventRepository.delete(event);
    }

    public Page<EventDto> listAllEvents(Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Page<Event> events = eventRepository.findAll(validatedPageable);

        if (events.isEmpty() && validatedPageable.getPageNumber() > 0) {
            throw new ApiException(
                    "No results for the requested page",
                    HttpStatus.NOT_FOUND,
                    StatusCodes.NO_RESULTS.name()
            );
        }

        return events.map(eventMapper::toDto);
    }

    public Page<EventDto> listEventsByInstructor(Long instructorId, Pageable pageable) {
        var instructor = entityLookupService.findUserById(instructorId);

        Pageable validatedPageable = paginationService.validatePageable(pageable);

        return eventRepository.findByInstructor(instructor, validatedPageable)
                .map(eventMapper::toDto);
    }

    public EventDto getEventById(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
        return eventMapper.toDto(event);
    }

    public void cancelEvent(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        event.setStatus(EventStatus.CANCELED);
        eventRepository.save(event);

        notificationService.notifyParticipants(event);
    }
}
