package pl.kamann.controllers.instructor;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.EventDto;
import pl.kamann.services.instructor.InstructorEventService;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorEventController {

    private final InstructorEventService instructorEventService;

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming events", description = "Retrieves a list of upcoming events for the instructor.")
    public ResponseEntity<List<EventDto>> getUpcomingEvents() {
        List<EventDto> events = instructorEventService.getUpcomingEventsForInstructor();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel event", description = "Allows an instructor to cancel an event assigned to them.")
    public ResponseEntity<Void> cancelAssignedEvent(@PathVariable Long eventId) {
        instructorEventService.cancelAssignedEvent(eventId);
        return ResponseEntity.ok().build();
    }
}
