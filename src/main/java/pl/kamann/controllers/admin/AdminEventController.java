package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.services.AppUserService;
import pl.kamann.services.admin.AdminEventService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;
    private final EntityLookupService lookupService;

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event with the provided details.")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        var createdBy = lookupService.getLoggedInUser();
        var instructor = lookupService.findUserById(eventDto.getInstructorId());
        var eventType = adminEventService.findEventTypeById(eventDto.getEventTypeId());
        return ResponseEntity.ok(adminEventService.createEvent(eventDto, createdBy, instructor, eventType));
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update an existing event", description = "Updates an existing event with the given details.")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody EventDto eventDto) {
        var instructor = lookupService.findUserById(eventDto.getInstructorId());
        var eventType = adminEventService.findEventTypeById(eventDto.getEventTypeId());
        return ResponseEntity.ok(adminEventService.updateEvent(eventId, eventDto, instructor, eventType));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event", description = "Deletes an event by its ID. Cannot delete events with participants.")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        adminEventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel an event", description = "Cancels an event if it has not started yet.")
    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<String> cancelEvent(@PathVariable Long eventId) {
        adminEventService.cancelEvent(eventId);
        return ResponseEntity.ok("Event successfully canceled.");
    }

    @GetMapping
    @Operation(summary = "List all events", description = "Retrieves a list of all events.")
    public ResponseEntity<List<EventDto>> listAllEvents() {
        return ResponseEntity.ok(adminEventService.listAllEvents());
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event details", description = "Retrieves detailed information about a specific event by its ID.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        var event = adminEventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }
}
