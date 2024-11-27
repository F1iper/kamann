package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.event.EventDto;
import pl.kamann.services.events.AdminEventService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        return ResponseEntity.ok(adminEventService.createEvent(eventDto));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody EventDto eventDto) {
        return ResponseEntity.ok(adminEventService.updateEvent(eventId, eventDto));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        adminEventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<String> cancelEvent(@PathVariable Long eventId) {
        try {
            adminEventService.cancelEvent(eventId);
            return ResponseEntity.ok("Event successfully canceled.");
        } catch (ApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> listAllEvents() {
        return ResponseEntity.ok(adminEventService.listAllEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        EventDto eventDto = adminEventService.getEventById(eventId);
        return ResponseEntity.ok(eventDto);
    }

}
