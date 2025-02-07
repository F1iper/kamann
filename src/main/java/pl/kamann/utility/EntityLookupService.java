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
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.*;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EntityLookupService {

    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final RoleRepository roleRepository;

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

    // todo: fix the logic, not throwing Api exception
    public void validateEmailNotTaken(String email) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new ApiException(
                    "Email is already registered: " + email,
                    HttpStatus.CONFLICT,
                    AuthCodes.EMAIL_ALREADY_EXISTS.name()
            );
        }
    }

    public Set<Role> findRolesByNameIn(Set<Role> roleNames) {
        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            throw new ApiException(
                    "No valid roles found for the provided names.",
                    HttpStatus.NOT_FOUND,
                    AuthCodes.ROLE_NOT_FOUND.name()
            );
        }
        return roles;
    }

    public EventType findEventTypeById(Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event type not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_TYPE_NOT_FOUND.name()
                ));
    }

    public AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found",
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
