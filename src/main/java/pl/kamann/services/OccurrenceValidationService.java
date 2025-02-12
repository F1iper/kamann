package pl.kamann.services;

import org.springframework.stereotype.Service;
import pl.kamann.entities.event.EventUpdateScope;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;

@Service
public class OccurrenceValidationService {

    public boolean isOccurrenceUpdatable(OccurrenceEvent occurrence, EventUpdateScope updateScope, long futurePeriodWeeks) {
        LocalDateTime now = LocalDateTime.now();
        return switch (updateScope) {
            case EVENT_ONLY -> false;
            case FUTURE_ONLY -> occurrence.getStart().isAfter(now);
            case FUTURE_FOR_PERIOD -> {
                LocalDateTime periodEnd = now.plusWeeks(futurePeriodWeeks);
                yield occurrence.getStart().isAfter(now) && occurrence.getStart().isBefore(periodEnd);
            }
            case ALL_OCCURRENCES -> true;
        };
    }
}
