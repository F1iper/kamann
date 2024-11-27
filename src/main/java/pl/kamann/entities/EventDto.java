package pl.kamann.entities;

import lombok.*;

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
    private Long createdById; // ID admina tworzącego wydarzenie
    private Long instructorId; // ID przypisanego instruktora
    private int maxParticipants;
    private EventStatus status;
    private Integer currentParticipants; // Liczba zapisanych uczestników
    private Long eventTypeId;
    private String eventTypeName;
}