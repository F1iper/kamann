package pl.kamann.dtos;

import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

public record EventUpdateResponse(

        Long id,
        String title,
        String description,
        LocalDateTime start,
        Integer durationMinutes,
        EventStatus status,
        LocalDateTime updatedAt,
        Long instructorId,
        Integer maxParticipants
) {
}
