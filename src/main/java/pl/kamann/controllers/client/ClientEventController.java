package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientEventService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@RestController
@RequestMapping("/api/client/events")
@RequiredArgsConstructor
@Tag(name = "/api/client/events", description = "Client events controller")
public class ClientEventController {

    private final ClientEventService clientEventService;
    private final ClientEventHistoryService clientEventHistoryService;
    private final EntityLookupService lookupService;

    @GetMapping("/available")
    @Operation(summary = "List available events", description = "Retrieves a list of events available for the client to join.")
    public ResponseEntity<List<EventDto>> getAvailableEvents() {
        List<EventDto> events = clientEventService.getAvailableEvents(lookupService.getLoggedInUser());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/registered")
    @Operation(summary = "List registered events", description = "Retrieves a list of events the client is registered for.")
    public ResponseEntity<List<EventDto>> getRegisteredEvents() {
        List<EventDto> events = clientEventService.getRegisteredEvents(lookupService.getLoggedInUser());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event details", description = "Retrieves details of a specific event by its ID.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        var event = clientEventService.getEventDetails(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/history")
    @Operation(summary = "Update event history", description = "Updates the event history for a client with the specified status.")
    public ResponseEntity<String> updateEventHistory(
            @PathVariable Long eventId,
            @RequestParam AttendanceStatus status) {
        var user = lookupService.getLoggedInUser();

        clientEventHistoryService.updateEventHistory(user, eventId, status);
        return ResponseEntity.ok("Event history updated successfully");
    }
}
