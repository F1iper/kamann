package pl.kamann.config.recurrence;

import pl.kamann.entities.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface RecurrenceStrategy {
    List<LocalDateTime> generateOccurrences(Event event, LocalDateTime until);
}