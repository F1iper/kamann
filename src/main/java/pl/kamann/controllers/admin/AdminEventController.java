package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.event.EventCancelResponse;
import pl.kamann.dtos.event.EventDto;
import pl.kamann.dtos.event.EventUpdateRequest;
import pl.kamann.dtos.event.EventUpdateResponse;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.services.admin.AdminEventService;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Tag(name = "3. admin event controller", description = "Control events and event occurences from admin perspective.")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @PostMapping
    @Operation(summary = "Create an event", description = "Creates a new event and assigns an instructor.")
    public ResponseEntity<CreateEventResponse> createEvent(
            @RequestBody @Valid CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminEventService.createEvent(request));
    }

    @GetMapping
    @Operation(
            summary = "List events",
            description = "Admins can list all events, or filter by instructor if an instructor ID is provided."
    )
    public ResponseEntity<PaginatedResponseDto<EventDto>> listEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminEventService.listEvents(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event details", description = "Retrieves detailed information about a specific event.")
    public ResponseEntity<EventDto> getEventDetails(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminEventService.getEventDtoById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update event details", description = "Updates event details partially – only fields provided in the request are updated.")
    public ResponseEntity<EventUpdateResponse> updateEventById(
            @PathVariable Long id,
            @RequestBody EventUpdateRequest requestDto
    ) {
        return ResponseEntity.ok(adminEventService.updateEvent(id, requestDto));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an event",
            description = "Cancels an event and notifies all participants."
    )
    public ResponseEntity<EventCancelResponse> cancelEvent(
            @PathVariable Long id) {
        adminEventService.cancelEvent(id, EventStatus.CANCELED);
        return ResponseEntity.ok(new EventCancelResponse(id, "Event successfully canceled."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event", description = "Deletes an event by its ID.")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id) {
        adminEventService.deleteEvent(id, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/force")
    @Operation(summary = "Force delete an event", description = "Deletes an event even if participants are registered.")
    public ResponseEntity<Void> forceDeleteEvent(
            @PathVariable Long id) {
        adminEventService.deleteEvent(id, true);
        return ResponseEntity.noContent().build();
    }
}
