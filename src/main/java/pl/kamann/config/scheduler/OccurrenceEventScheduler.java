package pl.kamann.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.admin.OccurrenceEventGeneratorService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OccurrenceEventScheduler {

    private final EventRepository eventRepository;
    private final OccurrenceEventRepository occurrenceRepository;
    private final OccurrenceEventGeneratorService generatorService;

    @Value("${scheduler.occurrence.until-months:2}")
    private int untilMonths;

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void generateOccurrenceEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusMonths(untilMonths);

        List<Event> events = eventRepository.findByRruleIsNotNullAndStartAfter(now);

        for (Event event : events) {
            boolean hasOccurrences = occurrenceRepository.existsByEventIdAndStartAfter(event.getId(), now);
            if (!hasOccurrences) {
                generatorService.generateOccurrencesForEvent(event, until);
            }
        }
    }
}