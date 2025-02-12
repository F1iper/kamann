package pl.kamann.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventUpdateRequestDto(
        @NotBlank String title,
        String description,
        @NotNull(message = "Start time must be provided")
        @FutureOrPresent(message = "Start time must be in the future or present")
        LocalDateTime start,
        @NotNull(message = "Duration must be provided")
        @Positive(message = "Duration must be positive")
        Integer durationMinutes,
        String rrule,
        Long instructorId,
        @NotNull Integer maxParticipants
) {
}