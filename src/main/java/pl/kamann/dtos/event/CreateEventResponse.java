package pl.kamann.dtos.event;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Schema(description = "DTO returned after event creation")
public record CreateEventResponse(

        @Schema(description = "Unique identifier of the created event", example = "1")
        Long id,

        @Schema(description = "Title of the event", example = "Salsa Beginner Class")
        String title,

        @Schema(description = "Start date and time of the event", example = "2025-02-09T16:14:01")
        LocalDateTime start,

        @Schema(description = "Duration of the event in minutes", example = "60")
        Integer durationMinutes,

        @Schema(description = "Current status of the event", example = "SCHEDULED")
        EventStatus status
) {}