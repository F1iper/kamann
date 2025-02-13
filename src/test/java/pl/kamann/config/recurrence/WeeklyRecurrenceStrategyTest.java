package pl.kamann.config.recurrence;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import pl.kamann.entities.event.Event;
import java.time.LocalDateTime;
import java.util.List;

class WeeklyRecurrenceStrategyTest {
    @Test
    void testGenerateOccurrences() {
        RecurrenceStrategy strategy = new WeeklyRecurrenceStrategy();
        LocalDateTime eventStart = LocalDateTime.of(2025, 2, 1, 10, 0);
        Event event = new Event();
        event.setStart(eventStart);
        LocalDateTime until = LocalDateTime.of(2025, 2, 15, 10, 0);
        List<LocalDateTime> occurrences = strategy.generateOccurrences(event, until);
        assertEquals(3, occurrences.size());
        assertEquals(eventStart, occurrences.get(0));
        assertEquals(eventStart.plusWeeks(1), occurrences.get(1));
        assertEquals(eventStart.plusWeeks(2), occurrences.get(2));
    }
}
