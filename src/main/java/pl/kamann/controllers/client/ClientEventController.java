package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.*;
import pl.kamann.dtos.event.EventDto;
import pl.kamann.dtos.event.EventLightDto;
import pl.kamann.services.client.ClientEventService;

@RestController
@RequestMapping("/api/v1/client")
@RequiredArgsConstructor
@Tag(name = "2. client event controller", description = "Fetch events and occurrences with filtering and pagination.")
public class ClientEventController {

    private final ClientEventService clientEventService;

    @GetMapping("/occurrences")
    @Operation(summary = "Get paginated occurrences", description = "Retrieves paginated occurrences based on scope.")
    public ResponseEntity<PaginatedResponseDto<OccurrenceEventLightDto>> getOccurrences(
            @RequestParam(defaultValue = "upcoming", required = false) OccurrenceEventScope scope,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(clientEventService.getOccurrences(scope, page, size));
    }

    @GetMapping("/occurrences/{occurrenceId}")
    @Operation(
            summary = "Get OccurrenceEvent details by ID",
            description = "Retrieve details of a specific OccurrenceEvent using its unique ID."
    )
    public ResponseEntity<OccurrenceEventDto> getOccurrenceEventById(@PathVariable Long occurrenceId) {
        return ResponseEntity.ok(clientEventService.getOccurrenceById(occurrenceId));
    }

    @GetMapping("/events")
    @Operation(summary = "Get paginated events", description = "Retrieves a paginated list of events.")
    public ResponseEntity<PaginatedResponseDto<EventLightDto>> getEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(clientEventService.getLightEvents(page, size));
    }

    @GetMapping("/events/{eventId}")
    @Operation(
            summary = "Get Event details by ID",
            description = "Retrieve details of a specific Event using its unique ID."
    )
    public ResponseEntity<EventDto> getEventById(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(clientEventService.getEventById(eventId));
    }

    @GetMapping("event-types/{eventType}/events")
    @Operation(summary = "Get events by event type", description = "Retrieves paginated events based on type")
    public ResponseEntity<PaginatedResponseDto<EventDto>> getEventsByType(
            @RequestParam String eventType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(clientEventService.getEventsByType(eventType, page, size));
    }
}