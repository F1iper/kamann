package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.dtos.EventDto;
import pl.kamann.services.client.ClientEventService;

import java.util.List;

@RestController
@RequestMapping("/api/client/events")
@RequiredArgsConstructor
@Tag(name = "Client Events", description = "Controller for managing high-level events for clients.")
public class ClientEventController {

    private final ClientEventService clientEventService;

    @GetMapping
    @Operation(summary = "List all events", description = "Retrieves a list of all high-level events.")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = clientEventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event details", description = "Retrieves details of a specific event by its ID.")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        EventDto event = clientEventService.getEventDetails(eventId);
        return ResponseEntity.ok(event);
    }
}
