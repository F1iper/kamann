package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventTypeRepository eventTypeRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;


    public EventDto createEvent(EventDto eventDto) {
        if (eventDto.createdById() == null) {
            throw new ApiException(
                    "Created by ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT
            );
        }

        if (eventDto.instructorId() == null) {
            throw new ApiException(
                    "Instructor ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT
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
                        Codes.EVENT_NOT_FOUND));

        var instructor = entityLookupService.findUserById(eventDto.instructorId());
        var eventType = entityLookupService.findEventTypeById(eventDto.eventTypeId());

        eventMapper.updateEventFromDto(existingEvent, eventDto);
        existingEvent.setInstructor(instructor);
        existingEvent.setEventType(eventType);

        return eventMapper.toDto(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id, boolean force) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException("Event not found", HttpStatus.NOT_FOUND, Codes.EVENT_NOT_FOUND));

        if (!force && !event.getAttendances().isEmpty()) {
            throw new ApiException(
                    "Cannot delete event with participants unless forced",
                    HttpStatus.BAD_REQUEST,
                    Codes.EVENT_HAS_PARTICIPANTS);
        }

        eventRepository.delete(event);
    }

    public Page<EventDto> listAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(eventMapper::toDto);
    }

    public Page<EventDto> listEventsByInstructor(Long instructorId, Pageable pageable) {
        var instructor = appUserRepository.findById(instructorId)
                .orElseThrow(() -> new ApiException(
                        "Instructor not found",
                        HttpStatus.NOT_FOUND,
                        Codes.INSTRUCTOR_NOT_FOUND));

        return eventRepository.findByInstructor(instructor, pageable).map(eventMapper::toDto);
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

    public void cancelEvent(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND));

        event.setStatus(EventStatus.CANCELED);
        eventRepository.save(event);

        notificationService.notifyParticipants(event);
    }
}
