package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventReportDto;
import pl.kamann.dtos.EventStat;

@Component
public class EventReportMapper {

    public EventReportDto toDto(EventStat stat) {
        return new EventReportDto(
                stat.getEventType(),
                stat.getTotalEvents(),
                stat.getCompletedEvents(),
                stat.getCancelledEvents()
        );
    }
}