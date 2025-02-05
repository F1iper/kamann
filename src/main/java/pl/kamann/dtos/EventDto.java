package pl.kamann.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        Long id,

        @NotBlank(message = "Title cannot be blank")
        String title,

        String description,

        @NotNull(message = "Start date/time cannot be null")
        @Future(message = "Start date/time must be in the future")
        LocalDateTime start,

        @NotNull(message = "Duration in minutes cannot be null")
        @Positive(message = "Duration must be positive")
        Integer durationMinutes,

        String rrule,

        @NotNull(message = "Creator ID cannot be null")
        Long createdById,

        Long instructorId,

        String instructorFullName,

        @PositiveOrZero(message = "Max participants must be zero or a positive number")
        Integer maxParticipants,

        @NotNull(message = "Event status cannot be null")
        EventStatus status,

        @PositiveOrZero(message = "Current participants must be zero or a positive number")
        int currentParticipants,

        @NotNull(message = "Event type ID cannot be null")
        Long eventTypeId,

        String eventTypeName
) {
        public EventDto {
                if (rrule != null && !rrule.isEmpty()) {
                        validateRRule(rrule);
                }
        }

        private void validateRRule(String rrule) {
                if (!rrule.startsWith("FREQ=")) {
                        throw new IllegalArgumentException("RRULE must start with FREQ=");
                        // todo handle other cases (if possible to receive from client)
                }
        }

        public LocalDateTime getEnd() {
                return start.plusMinutes(durationMinutes);
        }
}
