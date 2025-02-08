package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.EventResponseDto;
import pl.kamann.dtos.EventUpdateRequestDto;
import pl.kamann.entities.event.EventUpdateScope;
import pl.kamann.services.admin.AdminEventService;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@Tag(name = "3. admin events", description = "Auth controller")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    @Operation(
            summary = "List events",
            description = "Admins can list all events, or filter by instructor if an instructor ID is provided."
    )
    public ResponseEntity<PaginatedResponseDto<EventDto>> listEvents(
            @RequestParam(required = false) Long instructorId,
            Pageable pageable
    ) {
        PaginatedResponseDto<EventDto> response;
        if (instructorId == null) {
            response = adminEventService.listAllEvents(pageable);
        } else {
            response = adminEventService.listEventsByInstructor(instructorId, pageable);
        }
        return ResponseEntity.ok(response);
    }

//    @PostMapping
//    @Operation(
//            summary = "Create an event",
//            description = "Creates a new event and assigns an instructor."
//    )
//    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
//        EventDto createdEvent = adminEventService.createEvent(eventDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
//    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an event",
            description = "Updates event details. 'EVENT_ONLY' updates only the event metadata; " +
                    "'FUTURE_OCCURRENCES' updates event and future occurrences; " +
                    "'ALL_OCCURRENCES' updates event and all occurrences."
    )
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto requestDto,
            @RequestParam(name = "scope", defaultValue = "EVENT_ONLY") EventUpdateScope updateScope) {
        EventResponseDto updatedEvent = adminEventService.updateEvent(id, requestDto, updateScope);
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
        return ResponseEntity.ok(adminEventService.getEventById(eventId));
    }
}
