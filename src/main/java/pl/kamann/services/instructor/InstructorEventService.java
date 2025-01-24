package pl.kamann.services.instructor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EntityLookupService lookupService;

    public List<EventDto> getUpcomingEventsForInstructor() {
        var instructor = lookupService.getLoggedInUser();
        return eventRepository.findUpcomingEventsForInstructor(instructor.getId(), LocalDateTime.now()).stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public void cancelAssignedEvent(Long eventId) {
        var event = findEventById(eventId);
        var instructor = lookupService.getLoggedInUser();

        if (!event.getInstructor().equals(instructor)) {
            throw new ApiException("You are not assigned to this event.",
                    HttpStatus.FORBIDDEN,
                    AuthCodes.UNAUTHORIZED.name()
            );
        }

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Cannot cancel an event that has already started.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.CANNOT_CANCEL_STARTED_EVENT.name()
            );
        }

        event.setStatus(EventStatus.CANCELED);
        eventRepository.save(event);
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }
}
