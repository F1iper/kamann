package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.services.admin.AdminEventService;
import pl.kamann.services.client.ClientEventService;

@RestController
@RequestMapping("/api/client/occurrences")
@RequiredArgsConstructor
@Tag(name = "2. Client Occurrences", description = "Fetch occurrences with filtering and pagination.")
public class ClientEventController {

    private final ClientEventService clientEventService;
    private final AdminEventService adminEventService;

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

    @PostMapping
    @Operation(
            summary = "Create an event",
            description = "Creates a new event and assigns an instructor."
    )
    public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest request) {
        CreateEventResponse createdEvent = adminEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

}