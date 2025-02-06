package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.recurrence.RecurrenceStrategy;
import pl.kamann.config.recurrence.RecurrenceStrategyFactory;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.OccurrenceEventRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OccurrenceEventGeneratorService {

    private final OccurrenceEventRepository occurrenceEventRepository;

    @Transactional
    public void generateOccurrencesForEvent(Event event, LocalDateTime until) {
        RecurrenceStrategy strategy = RecurrenceStrategyFactory.getStrategy(event);
        if (strategy == null) return;

        List<LocalDateTime> dates = strategy.generateOccurrences(event, until);

        Set<LocalDateTime> existingOccurrences = new HashSet<>(
                occurrenceEventRepository.findStartDatesByEventId(event.getId())
        );

        dates.stream()
                .filter(date -> !existingOccurrences.contains(date))
                .map(date -> createOccurrence(event, date))
                .forEach(occurrenceEventRepository::save);
    }

    private OccurrenceEvent createOccurrence(Event event, LocalDateTime start) {
        return OccurrenceEvent.builder()
                .event(event)
                .start(start)
                .durationMinutes(event.getDurationMinutes())
                .maxParticipants(event.getMaxParticipants())
                .instructor(event.getInstructor())
                .seriesIndex(0)
                .build();
    }
}