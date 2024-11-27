package pl.kamann.entities.event;

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
    private Long createdById; // ID admina tworzącego wydarzenie
    private Long instructorId; // ID przypisanego instruktora
    private int maxParticipants;
    private EventStatus status;
    private Integer currentParticipants; // Liczba zapisanych uczestników
    private Long eventTypeId;
    private String eventTypeName;
}