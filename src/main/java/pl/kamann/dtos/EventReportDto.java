package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventReportDto {
    private String eventType;
    private long totalEvents;
    private long completedEvents;
    private long cancelledEvents;
}
