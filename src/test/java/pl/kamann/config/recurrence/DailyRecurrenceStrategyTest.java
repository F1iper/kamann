package pl.kamann.config.recurrence;

import org.junit.jupiter.api.Test;
import pl.kamann.entities.event.Event;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailyRecurrenceStrategyTest {

    @Test
    void testGenerateOccurrences_ShouldReturnCorrectDates() {
        RecurrenceStrategy strategy = new DailyRecurrenceStrategy();
        LocalDateTime eventStart = LocalDateTime.of(2025, 2, 1, 10, 0);
        Event event = new Event();
        event.setStart(eventStart);

        LocalDateTime until = LocalDateTime.of(2025, 2, 5, 10, 0);

        List<LocalDateTime> occurrences = strategy.generateOccurrences(event, until);

        assertEquals(5, occurrences.size());
        assertEquals(eventStart, occurrences.get(0));
        assertEquals(eventStart.plusDays(1), occurrences.get(1));
        assertEquals(eventStart.plusDays(4), occurrences.get(4));
    }

    @Test
    void testGenerateOccurrences_ExactMatchUntil() {
        RecurrenceStrategy strategy = new DailyRecurrenceStrategy();
        LocalDateTime eventStart = LocalDateTime.of(2025, 3, 1, 9, 0);
        Event event = new Event();
        event.setStart(eventStart);

        LocalDateTime until = eventStart;

        List<LocalDateTime> occurrences = strategy.generateOccurrences(event, until);

        assertEquals(1, occurrences.size());
        assertEquals(eventStart, occurrences.get(0));
    }

    @Test
    void testGenerateOccurrences_UntilBeforeEventStart_ShouldReturnEmpty() {
        RecurrenceStrategy strategy = new DailyRecurrenceStrategy();
        LocalDateTime eventStart = LocalDateTime.of(2025, 4, 1, 12, 0);
        Event event = new Event();
        event.setStart(eventStart);

        LocalDateTime until = LocalDateTime.of(2025, 3, 31, 12, 0);

        List<LocalDateTime> occurrences = strategy.generateOccurrences(event, until);

        assertTrue(occurrences.isEmpty());
    }
}