package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.services.client.ClientEventService;

@RestController
@RequestMapping("/api/client/occurrences")
@RequiredArgsConstructor
@Tag(name = "2. Client Occurrences", description = "Fetch occurrences with filtering and pagination.")
public class ClientEventController {

    private final ClientEventService clientEventService;

    @GetMapping
    @Operation(summary = "Get occurrences", description = "Retrieves paginated occurrences based on filter ('upcoming', 'past' or 'available' .")
    public ResponseEntity<PaginatedResponseDto<OccurrenceEventLightDto>> getOccurrences(
            @RequestParam(defaultValue = "upcoming") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").ascending());
        return ResponseEntity.ok(clientEventService.getOccurrences(filter, pageable));
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