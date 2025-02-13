package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.EventUpdateRequest;
import pl.kamann.dtos.EventUpdateResponse;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.services.EventUpdateService;
import pl.kamann.services.admin.AdminEventService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "3. admin event controller", description = "Control events and event occurences from admin perspective.")
public class AdminEventController {

    private final AdminEventService adminEventService;
    private final EventUpdateService eventUpdateService;

    @GetMapping("/events")
    @Operation(summary = "List events", description = "Admins can list all events, optionally filtering by instructor ID.")
    public ResponseEntity<PaginatedResponseDto<EventDto>> listEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminEventService.listEvents(page, size));
    }

    @PostMapping("/create")
    @Operation(summary = "Create an event", description = "Creates a new event and assigns an instructor.")
    public ResponseEntity<CreateEventResponse> createEvent(@RequestBody @Valid CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminEventService.createEvent(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch event details", description = "Updates event details partially – only fields provided in the request are updated.")
    public ResponseEntity<EventUpdateResponse> patchEventDetails(
            @PathVariable Long id,
            @RequestBody EventUpdateRequest requestDto
    ) {
        return ResponseEntity.ok(eventUpdateService.updateEventDetails(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event", description = "Deletes an event by its ID.")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminEventService.deleteEvent(id, false);
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

    @DeleteMapping("/{id}/force")
    @Operation(summary = "Force delete an event", description = "Deletes an event even if participants are registered.")
    public ResponseEntity<Void> forceDeleteEvent(@PathVariable Long id) {
        adminEventService.deleteEvent(id, true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event details", description = "Retrieves detailed information about a specific event.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminEventService.getEventDtoById(id));
    }
}
