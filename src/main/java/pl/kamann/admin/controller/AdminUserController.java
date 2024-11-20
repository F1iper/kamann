package pl.kamann.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.service.AuthService;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.service.EventService;
import pl.kamann.user.dto.AppUserDto;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.service.AppUserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AppUserService appUserService;
    private final AuthService authService;
    private final EventService eventService;

    // Manage Instructors
    @PostMapping("/instructors")
    public ResponseEntity<AppUserDto> addInstructor(@RequestParam Long userId) {
        AppUserDto updatedUser = appUserService.updateUserRoles(userId, Set.of(new Role("INSTRUCTOR")));
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/instructors/{id}")
    public ResponseEntity<AppUserDto> updateInstructor(@PathVariable Long id, @RequestBody AppUserDto userDto) {
        AppUserDto updatedUser = appUserService.updateUser(id, userDto); // Assume updateUser exists
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/instructors/{id}")
    public ResponseEntity<Void> removeInstructor(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserDto> createInstructor(@RequestBody @Valid RegisterRequest request) {
        AppUser instructor = authService.registerInstructor(request);
        AppUserDto response = appUserService.getUserById(instructor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/instructors/{id}/history")
    public ResponseEntity<List<EventDto>> getInstructorEventHistory(@PathVariable Long id) {
        List<EventDto> eventHistory = eventService.getEventsByInstructor(id);
        return ResponseEntity.ok(eventHistory);
    }

    // Manage Clients
    @GetMapping("/clients")
    public ResponseEntity<List<AppUserDto>> getAllClients() {
        List<AppUserDto> clients = appUserService.getUsersByRole("CLIENT");
        return ResponseEntity.ok(clients);
    }

    // Manage Events
    @PostMapping("/events")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        EventDto createdEvent = eventService.createEvent(eventDto);
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        EventDto updatedEvent = eventService.updateEvent(id, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
}