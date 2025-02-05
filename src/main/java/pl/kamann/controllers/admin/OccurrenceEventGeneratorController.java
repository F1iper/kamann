package pl.kamann.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.services.admin.OccurrenceEventGeneratorService;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/occurrence")
@RequiredArgsConstructor
public class OccurrenceEventGeneratorController {

    private final EventRepository eventRepository;

    private final OccurrenceEventGeneratorService generatorService;
    
    @PostMapping("/{eventId}")
    public ResponseEntity<?> generate(@PathVariable Long eventId,
                                      @RequestParam(value = "until", required = false) String untilStr) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        LocalDateTime until;
        if (untilStr != null && !untilStr.isEmpty()) {
            until = LocalDateTime.parse(untilStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            until = LocalDateTime.now().plusMonths(2);
        }
        generatorService.generateOccurrencesForEvent(event, until);
        return ResponseEntity.ok().build();
    }
}
