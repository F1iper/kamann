package pl.kamann.services;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.stereotype.Service;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class OccurrenceService {

    public List<OccurrenceEvent> generateOccurrences(Event event) {
        if (!event.getRecurring() || event.getRecurrenceRule() == null) {
            return List.of(createSingleOccurrence(event));
        }

        List<OccurrenceEvent> occurrences = new ArrayList<>();
        RecurrenceRule rule = event.getRecurrenceRule();
        DateTime startDateTime = convertToDateTime(event.getStartDate());

        RecurrenceRuleIterator it = rule.iterator(startDateTime);
        int index = 0;

        while (it.hasNext() && isUnderLimit(event, index)) {
            DateTime occurrenceDateTime = it.nextDateTime();
            LocalDate occurrenceDate = convertToLocalDate(occurrenceDateTime);

            if (isExcluded(event, occurrenceDate)) continue;
            if (isAfterEndDate(event, occurrenceDate)) break;

            occurrences.add(buildOccurrence(event, occurrenceDate, index++));
        }

        return occurrences;
    }

    private OccurrenceEvent createSingleOccurrence(Event event) {
        return buildOccurrence(event, event.getStartDate(), 0);
    }

    private boolean isUnderLimit(Event event, int index) {
        return event.getOccurrenceLimit() == null || index < event.getOccurrenceLimit();
    }

    private boolean isExcluded(Event event, LocalDate date) {
        return event.getExdates() != null && event.getExdates().contains(date);
    }

    private boolean isAfterEndDate(Event event, LocalDate date) {
        return event.getRecurrenceEndDate() != null && date.isAfter(event.getRecurrenceEndDate());
    }

    private DateTime convertToDateTime(LocalDate date) {
        long epochMilli = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new DateTime(epochMilli);
    }

    private LocalDate convertToLocalDate(DateTime dateTime) {
        return Instant.ofEpochMilli(dateTime.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private OccurrenceEvent buildOccurrence(Event event, LocalDate date, int index) {
        return OccurrenceEvent.builder()
                .event(event)
                .date(date)
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .seriesIndex(index)
                .canceled(false)
                .createdBy(event.getCreatedBy())
                .instructor(event.getInstructor())
                .build();
    }
}
