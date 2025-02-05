package pl.kamann.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.EventRepository;
import pl.kamann.services.admin.OccurrenceEventGeneratorService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OccurrenceEventScheduler {

    private final EventRepository eventRepository;

    private final OccurrenceEventGeneratorService generatorService;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateOccurrenceEvents() {
        List<Event> events = eventRepository.findByRruleIsNotNull();
        LocalDateTime until = LocalDateTime.now().plusMonths(2);
        for (Event event : events) {
            generatorService.generateOccurrencesForEvent(event, until);
        }
    }
}
