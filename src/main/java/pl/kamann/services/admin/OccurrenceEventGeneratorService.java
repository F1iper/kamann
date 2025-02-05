package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.config.recurrence.RecurrenceStrategy;
import pl.kamann.config.recurrence.RecurrenceStrategyFactory;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.OccurrenceEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OccurrenceEventGeneratorService {

    private final OccurrenceEventRepository occurrenceEventRepository;

    public void generateOccurrencesForEvent(Event event, LocalDateTime until) {
        RecurrenceStrategy strategy = RecurrenceStrategyFactory.getStrategy(event);
        if (strategy == null) return;
        List<LocalDateTime> dates = strategy.generateOccurrences(event, until);
        for (LocalDateTime date : dates) {
            if (!occurrenceEventRepository.findByEventIdAndStart(event.getId(), date).isPresent()) {
                OccurrenceEvent occurrence = new OccurrenceEvent();
                occurrence.setEvent(event);
                occurrence.setStart(date);
                occurrence.setDurationMinutes(event.getDurationMinutes());
                occurrence.setMaxParticipants(event.getMaxParticipants());
                occurrence.setInstructor(event.getInstructor());
                occurrence.setSeriesIndex(0);
                occurrenceEventRepository.save(occurrence);
            }
        }
    }
}
