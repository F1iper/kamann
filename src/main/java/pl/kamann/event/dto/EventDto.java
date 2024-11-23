package pl.kamann.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.event.model.EventStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean recurring;
    private Long createdById;
    private Long instructorId;
    private int maxParticipants;
    private Long eventTypeId;
    private String eventTypeName;
    private EventStatus status;
    private Integer attendanceSummary;
}