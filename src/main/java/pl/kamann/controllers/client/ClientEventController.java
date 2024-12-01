package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.EventDto;
import pl.kamann.services.client.ClientEventService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientEventController {

    private final ClientEventService clientEventService;
    private EntityLookupService lookupService;

    @GetMapping("/events/available")
    @Operation(summary = "List available events", description = "Retrieves a list of events available for the client to join.")
    public ResponseEntity<List<EventDto>> getAvailableEvents() {
        List<EventDto> events = clientEventService.getAvailableEvents(lookupService.getLoggedInUser());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/registered")
    @Operation(summary = "List registered events", description = "Retrieves a list of events the client is registered for.")
    public ResponseEntity<List<EventDto>> getRegisteredEvents() {
        List<EventDto> events = clientEventService.getRegisteredEvents(lookupService.getLoggedInUser());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Get event details", description = "Retrieves details of a specific event by its ID.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        var event = clientEventService.getEventDetails(eventId);
        return ResponseEntity.ok(event);
    }
}
