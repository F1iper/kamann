package pl.kamann.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.kamann.entities.Role;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventType;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EntityLookupService {

    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final RoleRepository roleRepository;

    public AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found with ID: " + userId,
                        HttpStatus.NOT_FOUND,
                        Codes.USER_NOT_FOUND
                ));
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND
                ));
    }

    public void validateEventStartTime(Event event) {
        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Cannot register for past events.",
                    HttpStatus.BAD_REQUEST,
                    Codes.PAST_EVENT_ERROR
            );
        }
    }

    public void validateUserRegistration(AppUser user, Event event, boolean exists) {
        boolean isRegistered = event.getParticipants().contains(user);
        if (exists && !isRegistered) {
            throw new ApiException(
                    "User is not registered for this event.",
                    HttpStatus.NOT_FOUND,
                    Codes.REGISTRATION_NOT_FOUND
            );
        } else if (!exists && isRegistered) {
            throw new ApiException(
                    "User is already registered for this event.",
                    HttpStatus.CONFLICT,
                    Codes.ALREADY_REGISTERED
            );
        }
    }

    public EventType findEventTypeByName(String eventTypeName) {
        return eventTypeRepository.findByName(eventTypeName)
                .orElseThrow(() -> new ApiException(
                        "Event type not found: " + eventTypeName,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_TYPE_NOT_FOUND
                ));
    }

    public void validateEventTypeExists(String eventTypeName) {
        if (!eventTypeRepository.existsByName(eventTypeName)) {
            throw new ApiException(
                    "Event type not found: " + eventTypeName,
                    HttpStatus.NOT_FOUND,
                    Codes.EVENT_TYPE_NOT_FOUND
            );
        }
    }

    // todo: fix the logic, not throwing Api exception
    public void validateEmailNotTaken(String email) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new ApiException(
                    "Email is already registered: " + email,
                    HttpStatus.CONFLICT,
                    Codes.EMAIL_ALREADY_EXISTS
            );
        }
    }

    public Set<Role> findRolesByNameIn(Set<Role> roleNames) {
        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            throw new ApiException(
                    "No valid roles found for the provided names.",
                    HttpStatus.NOT_FOUND,
                    Codes.ROLE_NOT_FOUND
            );
        }
        return roles;
    }

    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND
                ));
    }

    public EventType findEventTypeById(Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event type not found",
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_TYPE_NOT_FOUND));
    }

    public AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
    }

    public AppUser getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findUserByEmail(email);
    }

}
