package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.EventDto;
import pl.kamann.services.admin.AdminEventService;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    @Operation(
            summary = "List events",
            description = "Admins can list all events, or filter by instructor if an instructor ID is provided."
    )
    public ResponseEntity<Page<EventDto>> listEvents(
            @RequestParam(required = false) Long instructorId,
            Pageable pageable
    ) {
        if (instructorId == null) {
            return ResponseEntity.ok(adminEventService.listAllEvents(pageable));
        }
        return ResponseEntity.ok(adminEventService.listEventsByInstructor(instructorId, pageable));
    }

    @PostMapping
    @Operation(
            summary = "Create an event",
            description = "Creates a new event and assigns an instructor."
    )
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        EventDto createdEvent = adminEventService.createEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an event",
            description = "Updates the details of an event and can reassign its instructor."
    )
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        EventDto updatedEvent = adminEventService.updateEvent(id, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an event",
            description = "Deletes an event by its ID. If participants are registered, deletion requires force=true."
    )
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        adminEventService.deleteEvent(id, force);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an event",
            description = "Cancels an event and notifies all participants."
    )
    public ResponseEntity<String> cancelEvent(@PathVariable Long id) {
        adminEventService.cancelEvent(id);
        return ResponseEntity.ok("Event successfully canceled.");
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event details", description = "Retrieves detailed information about a specific event by its ID.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        var event = adminEventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }
}
