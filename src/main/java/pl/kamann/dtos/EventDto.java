package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Getter
@Setter
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
    private EventStatus status;
    private Integer currentParticipants;
    private Long eventTypeId;
    private String eventTypeName;
}