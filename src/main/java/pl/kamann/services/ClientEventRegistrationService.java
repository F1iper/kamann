package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.Event;
import pl.kamann.dtos.UserEventRegistrationDto;
import pl.kamann.mappers.UserEventRegistrationMapper;
import pl.kamann.entities.UserEventRegistration;
import pl.kamann.entities.UserEventRegistrationStatus;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventRegistrationService {

    private final UserEventRegistrationRepository registrationRepository;
    private final UserEventRegistrationMapper userEventRegistrationMapper;
    private final UserEventRegistrationRepository userEventRegistrationRepository;
    private final EntityLookupService lookupService;

    /**
     * Registers a user for an event. Handles waitlist logic if the event is full.
     *
     * @param userId  The ID of the user registering.
     * @param eventId The ID of the event.
     * @return true if registration is successful.
     */
    public boolean registerUserForEvent(Long userId, Long eventId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Cannot register for a past event.",
                    HttpStatus.BAD_REQUEST,
                    Codes.PAST_EVENT_ERROR
            );
        }

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new ApiException(
                    "User is already registered for this event.",
                    HttpStatus.CONFLICT,
                    Codes.ALREADY_REGISTERED
            );
        }

        UserEventRegistration registration = new UserEventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());

        int registeredCount = registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED);
        if (registeredCount < event.getMaxParticipants()) {
            registration.setStatus(UserEventRegistrationStatus.REGISTERED);
            registration.setWaitlistPosition(null);
        } else {
            handleWaitlistRegistration(event, registration);
        }

        registrationRepository.save(registration);
        return true;
    }

    /**
     * Cancels a user's registration for an event and updates the waitlist.
     *
     * @param userId  The ID of the user.
     * @param eventId The ID of the event.
     * @return true if cancellation is successful.
     */
    public boolean cancelUserRegistration(Long userId, Long eventId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        UserEventRegistration registration = registrationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException(
                        "Registration not found for user ID: " + userId + " and event ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.REGISTRATION_NOT_FOUND
                ));

        registration.setStatus(UserEventRegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        handleWaitlist(eventId);
        return true;
    }

    /**
     * Updates the waitlist for an event by promoting the next waitlisted user to registered.
     *
     * @param eventId The ID of the event.
     */
    public void handleWaitlist(Long eventId) {
        Event event = lookupService.findEventById(eventId);

        UserEventRegistration waitlistedUser = registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(
                event, UserEventRegistrationStatus.WAITLISTED);

        if (waitlistedUser != null) {
            waitlistedUser.setStatus(UserEventRegistrationStatus.REGISTERED);
            waitlistedUser.setWaitlistPosition(null);
            registrationRepository.save(waitlistedUser);
        }
    }

    /**
     * Retrieves the waitlist for an event.
     *
     * @param eventId The ID of the event.
     * @return List of waitlisted registrations.
     */
    public List<UserEventRegistration> getWaitlistForEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);

        return registrationRepository.findAllByEventAndStatusOrderByWaitlistPositionAsc(event, UserEventRegistrationStatus.WAITLISTED);
    }

    /**
     * Checks if a user is already registered for an event.
     *
     * @param userId  The ID of the user.
     * @param eventId The ID of the event.
     * @return true if the user is registered.
     */
    public boolean isUserRegisteredForEvent(Long userId, Long eventId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);

        return registrationRepository.existsByUserAndEvent(user, event);
    }

    /**
     * Handles the logic for adding a user to the waitlist.
     *
     * @param event        The event.
     * @param registration The user registration.
     */
    private void handleWaitlistRegistration(Event event, UserEventRegistration registration) {
        int waitlistSize = registrationRepository.countByEventAndWaitlistPositionIsNotNull(event);
        registration.setStatus(UserEventRegistrationStatus.WAITLISTED);
        registration.setWaitlistPosition(waitlistSize + 1);
    }

    public UserEventRegistrationDto getRegistrationById(Long registrationId) {
        UserEventRegistration registration = userEventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException("Registration not found", HttpStatus.NOT_FOUND, "REGISTRATION_NOT_FOUND"));
        return userEventRegistrationMapper.toDto(registration);
    }

    public UserEventRegistrationDto addRegistration(UserEventRegistrationDto dto, Long userId, Long eventId) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        UserEventRegistration registration = userEventRegistrationMapper.toEntity(dto, user, event);
        return userEventRegistrationMapper.toDto(userEventRegistrationRepository.save(registration));
    }
}
