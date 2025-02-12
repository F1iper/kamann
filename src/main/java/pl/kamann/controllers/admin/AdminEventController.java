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
import pl.kamann.dtos.EventResponseDto;
import pl.kamann.dtos.EventUpdateRequestDto;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.EventUpdateScope;
import pl.kamann.services.admin.AdminEventService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "3. admin event controller", description = "Control events and event occurences from admin perspective.")
public class AdminEventController {

    private final AdminEventService adminEventService;

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

    @PutMapping("/{id}")
    @Operation(summary = "Update an event", description = "Updates event details based on the given scope.")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto requestDto,
            @RequestParam(name = "scope", defaultValue = "EVENT_ONLY") EventUpdateScope updateScope,
            @RequestParam(name = "futurePeriodWeeks", defaultValue = "1") long futurePeriodWeeks
    ) {
        return ResponseEntity.ok(adminEventService.updateEvent(id, requestDto, updateScope, futurePeriodWeeks));
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
