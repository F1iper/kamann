package pl.kamann.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        @NotNull(message = "Event ID cannot be null")
        Long id,

        @NotBlank(message = "Title cannot be blank")
        String title,

        String description,

        @NotNull(message = "Start time cannot be null")
        @Future(message = "Start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time cannot be null")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        boolean recurring,

        @NotNull(message = "Creator ID cannot be null")
        Long createdById,

        Long instructorId,

        @PositiveOrZero(message = "Max participants must be zero or a positive number")
        int maxParticipants,

        @NotNull(message = "Event status cannot be null")
        EventStatus status,

        @PositiveOrZero(message = "Current participants must be zero or a positive number")
        Integer currentParticipants,

        Long eventTypeId,

        String eventTypeName
) {}
