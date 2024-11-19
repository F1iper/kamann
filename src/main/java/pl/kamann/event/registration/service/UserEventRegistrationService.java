package pl.kamann.event.registration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.specific.EventNotFoundException;
import pl.kamann.config.exception.specific.RegistrationNotFoundException;
import pl.kamann.event.model.Event;
import pl.kamann.event.registration.model.UserEventRegistration;
import pl.kamann.event.registration.model.UserEventRegistrationStatus;
import pl.kamann.event.registration.repository.UserEventRegistrationRepository;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserEventRegistrationService {

    private final EventRepository eventRepository;
    private final UserEventRegistrationRepository registrationRepository;

    public boolean registerUserForEvent(AppUser user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot register for past events.");
        }

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalArgumentException("User is already registered for this event.");
        }

        UserEventRegistration registration = new UserEventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());

        if (event.getMaxParticipants() <= registrationRepository.countByEvent(event)) {
            //todo: waitlist not implemented yet
//            registration.setStatus("WAITLISTED");
        } else {
            registration.setStatus(UserEventRegistrationStatus.REGISTERED);
        }

        registrationRepository.save(registration);
        return true;
    }

    public boolean cancelUserRegistration(AppUser user, Long eventId) {
        UserEventRegistration registration = registrationRepository.findByUserAndEvent_Id(user, eventId)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found"));

        Event event = registration.getEvent();

        if (event.getStartTime().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel registration less than 1 hour before the event.");
        }

        registration.setStatus(UserEventRegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
        return true;
    }
}