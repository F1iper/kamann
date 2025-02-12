package pl.kamann.dtos.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;

import java.time.LocalDateTime;

@Builder
@Schema(description = "DTO used for creating an event")
public record CreateEventRequest(
        @NotBlank(message = "Title cannot be blank")
        @Schema(description = "Title of the event", example = "Salsa Beginner Class")
        String title,

        @Schema(description = "Optional description of the event", example = "A beginner-level salsa class")
        String description,

        @NotNull(message = "Start date/time cannot be null")
        @Future(message = "Start date/time must be in the future")
        @Schema(description = "Start date and time of the event (must be in the future)", example = "2025-02-09T16:14:01")
        LocalDateTime start,

        @NotNull(message = "Duration in minutes cannot be null")
        @Positive(message = "Duration must be positive")
        @Schema(description = "Duration of the event in minutes", example = "60")
        Integer durationMinutes,

        @Schema(description = "Recurrence rule for repeating events (RFC 5545 format)", example = "FREQ=WEEKLY;BYDAY=MO,WE,FR")
        String rrule,

        @NotNull(message = "Creator ID cannot be null")
        @Schema(description = "ID of the user who created the event", example = "1")
        Long createdById,

        @Schema(description = "ID of the instructor for the event (nullable if no instructor assigned)", example = "2")
        Long instructorId,

        @PositiveOrZero(message = "Max participants must be zero or a positive number")
        @Schema(description = "Maximum number of participants allowed (0 for unlimited)", example = "20")
        Integer maxParticipants,

        @Schema(description = "Name of the event type", example = "dance")
        String eventTypeName
) {
        public CreateEventRequest {
                if (rrule != null && !rrule.isEmpty()) {
                        validateRRule(rrule);
                }
        }

        private void validateRRule(String rrule) {
                if (!rrule.startsWith("FREQ=")) {
                        throw new ApiException("RRULE must start with FREQ=",
                                HttpStatus.BAD_REQUEST,
                                EventCodes.OCCURRENCE_GENERATION_FAILED.name());
                }
        }
}