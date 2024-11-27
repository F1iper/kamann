package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.Event;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

@Service
@RequiredArgsConstructor
public class InstructorEventService {

    private final EventService eventService;
    private final EntityLookupService lookupService;

    @Transactional
    public void cancelAssignedEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);

        AppUser instructor = lookupService.getLoggedInUser();
        if (!event.getInstructor().equals(instructor)) {
            throw new ApiException("You are not assigned to this event.",
                    HttpStatus.FORBIDDEN, Codes.UNAUTHORIZED);
        }

        eventService.cancelEvent(eventId);
    }
}
