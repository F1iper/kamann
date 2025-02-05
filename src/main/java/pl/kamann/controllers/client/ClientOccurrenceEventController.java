package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.services.OccurrenceService;
import pl.kamann.services.client.ClientEventService;

import java.util.List;

@RestController
@RequestMapping("/api/client/occurrences")
@RequiredArgsConstructor
@Tag(name = "2. client events", description = "Controller for viewing available and enrolled event occurrences.")
public class ClientOccurrenceEventController {

    private final OccurrenceService occurrenceService;
    private final ClientEventService clientEventService;

    @GetMapping("/upcoming")
    public List<OccurrenceEventLightDto> getUpcomingEvents() {
        return clientEventService.getUpcomingEvents();
    }

    @GetMapping("/available")
    @Operation(summary = "List available occurrences",
            description = "Retrieves a list of all upcoming occurrences that are available for enrollment.")
    public ResponseEntity<List<OccurrenceEventDto>> getAvailableOccurrences() {
        List<OccurrenceEventDto> occurrences = occurrenceService.getAvailableOccurrences();
        return ResponseEntity.ok(occurrences);
    }

    @GetMapping("/enrolled")
    @Operation(summary = "List enrolled occurrences",
            description = "Retrieves a list of occurrences the current user is enrolled in.")
    public ResponseEntity<List<OccurrenceEventDto>> getEnrolledOccurrences() {
        List<OccurrenceEventDto> occurrences = occurrenceService.getUserEnrolledOccurrences();
        return ResponseEntity.ok(occurrences);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get occurrence details",
            description = "Retrieves details of a specific occurrence if the user has access to view it.")
    public ResponseEntity<OccurrenceEventDto> getOccurrenceDetails(@PathVariable Long id) {
        OccurrenceEventDto occurrence = occurrenceService.getOccurrenceDetailsForUser(id);
        return ResponseEntity.ok(occurrence);
    }
}