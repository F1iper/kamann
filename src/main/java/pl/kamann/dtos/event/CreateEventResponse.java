package pl.kamann.dtos.event;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Schema(description = "DTO returned after event creation")
public record CreateEventResponse(

        Long id,
        String title,
        LocalDateTime start,
        Integer durationMinutes,
        EventStatus status
) {
}