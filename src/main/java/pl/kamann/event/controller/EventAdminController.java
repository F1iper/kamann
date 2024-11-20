package pl.kamann.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.service.EventService;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('" + Codes.ADMIN + "')")
@RestController
@RequestMapping("api/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @GetMapping("/search")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<EventDto> events = eventService.searchEvents(startDate, endDate, instructorId, eventType, keyword, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {
        EventDto eventDto = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDto);
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody @Valid EventDto eventDto) {
        EventDto createdEvent = eventService.createEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventDto eventDto) {
        EventDto updatedEvent = eventService.updateEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}