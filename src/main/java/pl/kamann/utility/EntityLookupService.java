package pl.kamann.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AuthUserRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;

@Service
@RequiredArgsConstructor
public class EntityLookupService {

    private final AppUserRepository appUserRepository;
    private final AuthUserRepository authUserRepository;
    private final EventRepository eventRepository;
    private final OccurrenceEventRepository occurrenceEventRepository;

    public AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found with ID: " + userId,
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()
                ));
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }

    public void validateEmailNotTaken(String email) {
        if (authUserRepository.findByEmail(email).isPresent()) {
            throw new ApiException(
                    "Email is already registered: " + email,
                    HttpStatus.CONFLICT,
                    AuthCodes.EMAIL_ALREADY_EXISTS.name()
            );
        }
    }

    public AppUser findUserByEmail(String email) {
        return authUserRepository.findByEmail(email)
                .map(authUser -> {
                    AppUser appUser = authUser.getAppUser();
                    if (appUser == null) {
                        throw new ApiException(
                                "AppUser not found for AuthUser with email: " + email,
                                HttpStatus.NOT_FOUND,
                                AuthCodes.USER_NOT_FOUND.name());
                    }
                    return appUser;
                })
                .orElseThrow(() -> new ApiException(
                        "User not found with email: " + email,
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()));
    }

    public AppUser getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findUserByEmail(email);
    }

    public OccurrenceEvent findOccurrenceEventByOccurrenceEventId(Long occurrenceEventId) {
        return occurrenceEventRepository.findById(occurrenceEventId)
                .orElseThrow(() -> new ApiException(
                        "OccurrenceEvent not found for ID: " + occurrenceEventId,
                        HttpStatus.NOT_FOUND,
                        AttendanceCodes.OCCURRENCE_EVENT_NOT_FOUND.name()
                ));
    }
}
