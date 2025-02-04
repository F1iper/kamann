package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.services.client.ClientEventService;
import pl.kamann.utility.EntityLookupService;

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

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new high-level event.")
    public ResponseEntity<EventDto> createEvent(@RequestBody @Valid EventDto eventDto) {
        EventDto createdEvent = clientEventService.createEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update an event", description = "Updates an existing high-level event.")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventDto eventDto) {
        EventDto updatedEvent = clientEventService.updateEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event", description = "Deletes a high-level event and all its occurrences.")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        clientEventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
