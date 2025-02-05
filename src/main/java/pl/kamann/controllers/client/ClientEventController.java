package pl.kamann.controllers.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.services.client.ClientEventService;

import java.util.List;

@RestController
@RequestMapping("/api/client/events")
@RequiredArgsConstructor
public class ClientEventController {
    private final ClientEventService clientEventService;

    @GetMapping("/upcoming")
    public List<OccurrenceEventLightDto> getUpcomingEvents() {
        return clientEventService.getUpcomingEvents();
    }

    @GetMapping("/registered")
    public List<OccurrenceEventLightDto> getRegisteredEvents(@RequestParam Long userId) {
        return clientEventService.getRegisteredEvents(userId);
    }

    @GetMapping("/past")
    public List<OccurrenceEventLightDto> getPastEvents() {
        return clientEventService.getPastEvents();
    }
}