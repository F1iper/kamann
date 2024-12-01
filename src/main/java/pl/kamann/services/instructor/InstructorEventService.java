package pl.kamann.services.instructor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventDto;
import pl.kamann.entities.EventStatus;
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
                    Codes.UNAUTHORIZED);
        }

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

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND));
    }
}
