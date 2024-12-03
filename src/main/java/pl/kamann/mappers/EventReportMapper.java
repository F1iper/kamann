package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.reports.EventReportDto;
import pl.kamann.entities.reports.EventStatEntity;

@Component
public class EventReportMapper {

    public EventReportDto toDto(EventStatEntity stat) {
        return new EventReportDto(
                stat.getEventType(),
                stat.getTotalEvents(),
                stat.getCompletedEvents(),
                stat.getCancelledEvents()
        );
    }
}