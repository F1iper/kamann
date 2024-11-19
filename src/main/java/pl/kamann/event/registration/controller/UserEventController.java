package pl.kamann.event.registration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.event.registration.service.UserEventRegistrationService;
import pl.kamann.event.service.EventService;
import pl.kamann.user.model.AppUser;

@RestController
@RequestMapping("/api/user/events")
@RequiredArgsConstructor
public class UserEventController {

    private final EventService eventService;
    private final UserEventRegistrationService registrationService;

    @PostMapping("/{eventId}/register")
    public ResponseEntity<Void> registerForEvent(@PathVariable Long eventId, @RequestBody AppUser user) {
        boolean registered = registrationService.registerUserForEvent(user, eventId);
        return registered ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId, @RequestBody AppUser user) {
        boolean cancelled = registrationService.cancelUserRegistration(user, eventId);
        return cancelled ? ResponseEntity.status(HttpStatus.OK).build() : ResponseEntity.notFound().build();
    }
}