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
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.service.EventService;
import pl.kamann.user.dto.AppUserDto;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.service.AppUserService;

import java.util.List;
import java.util.Set;

@PreAuthorize("hasRole('" + Codes.ADMIN + "')")
@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AppUserAdminController {

    private final AppUserService appUserService;
    private final AuthService authService;
    private final EventService eventService;

    @PostMapping("/instructors/assign")
    public ResponseEntity<AppUserDto> assignInstructorRole(@RequestParam Long userId) {
        AppUserDto updatedUser = appUserService.updateUserRoles(userId, Set.of(new Role("INSTRUCTOR")));
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/instructors/create")
        public ResponseEntity<AppUserDto> createInstructor(@RequestBody @Valid RegisterRequest request) {
        AppUser instructor = authService.registerInstructor(request);
        AppUserDto response = appUserService.getUserById(instructor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/instructors/{id}")
    public ResponseEntity<AppUserDto> updateInstructor(@PathVariable Long id, @RequestBody @Valid AppUserDto userDto) {
        AppUserDto updatedUser = appUserService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/instructors/{id}")
    public ResponseEntity<Void> removeInstructor(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/instructors/{id}/history")
    public ResponseEntity<List<EventDto>> getInstructorEventHistory(@PathVariable Long id) {
        List<EventDto> eventHistory = eventService.getEventsByInstructor(id);
        return ResponseEntity.ok(eventHistory);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<AppUserDto>> getAllClients() {
        List<AppUserDto> clients = appUserService.getUsersByRole("CLIENT");
        return ResponseEntity.ok(clients);
    }
}