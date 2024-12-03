package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStat {
    private String eventType;
    private long totalEvents;
    private long completedEvents;
    private long cancelledEvents;
}
