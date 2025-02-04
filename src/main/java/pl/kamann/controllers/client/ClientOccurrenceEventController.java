package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.services.OccurrenceService;

import java.util.List;

@RestController
@RequestMapping("/api/client/occurrences")
@RequiredArgsConstructor
@Tag(name = "Client Occurrences", description = "Controller for managing specific occurrences of events for clients.")
public class ClientOccurrenceEventController {

    private final OccurrenceService occurrenceService;

    @GetMapping
    @Operation(summary = "List all occurrences", description = "Retrieves a list of all occurrences.")
    public ResponseEntity<List<OccurrenceEventDto>> getAllOccurrences() {
        List<OccurrenceEventDto> occurrences = occurrenceService.getAllOccurrences();
        return ResponseEntity.ok(occurrences);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get occurrence details", description = "Retrieves details of a specific occurrence by its ID.")
    public ResponseEntity<OccurrenceEventDto> getOccurrenceById(@PathVariable Long id) {
        OccurrenceEventDto occurrence = occurrenceService.getOccurrenceById(id);
        return ResponseEntity.ok(occurrence);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an occurrence", description = "Updates an existing occurrence (e.g., cancel or reschedule).")
    public ResponseEntity<OccurrenceEventDto> updateOccurrence(
            @PathVariable Long id,
            @RequestBody @Valid OccurrenceEventDto occurrenceDto) {
        OccurrenceEventDto updatedOccurrence = occurrenceService.updateOccurrence(id, occurrenceDto);
        return ResponseEntity.ok(updatedOccurrence);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an occurrence", description = "Deletes a specific occurrence without affecting the parent event.")
    public ResponseEntity<Void> deleteOccurrence(@PathVariable Long id) {
        occurrenceService.deleteOccurrence(id);
        return ResponseEntity.noContent().build();
    }
}