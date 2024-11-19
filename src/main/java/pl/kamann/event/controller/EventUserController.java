package pl.kamann.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.Rating;
import pl.kamann.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/user/events")
@RequiredArgsConstructor
public class EventUserController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }

    //todo: for future implementation (19.11.2024)
//    @PostMapping("/{eventId}/rate")
//    public ResponseEntity<Void> rateEvent(@PathVariable Long eventId, @RequestBody Rating rating) {
//        boolean success = ratingService.addRating(rating);
//        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
//    }
}