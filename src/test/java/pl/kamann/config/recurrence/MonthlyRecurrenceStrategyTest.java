package pl.kamann.config.recurrence;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import pl.kamann.entities.event.Event;
import java.time.LocalDateTime;
import java.util.List;

class MonthlyRecurrenceStrategyTest {
    @Test
    void testGenerateOccurrences() {
        RecurrenceStrategy strategy = new MonthlyRecurrenceStrategy();
        LocalDateTime eventStart = LocalDateTime.of(2025, 1, 1, 9, 0);
        Event event = new Event();
        event.setStart(eventStart);
        LocalDateTime until = LocalDateTime.of(2025, 4, 1, 9, 0);
        List<LocalDateTime> occurrences = strategy.generateOccurrences(event, until);
        assertEquals(4, occurrences.size());
        assertEquals(eventStart, occurrences.get(0));
        assertEquals(eventStart.plusMonths(1), occurrences.get(1));
        assertEquals(eventStart.plusMonths(2), occurrences.get(2));
        assertEquals(eventStart.plusMonths(3), occurrences.get(3));
    }
}
