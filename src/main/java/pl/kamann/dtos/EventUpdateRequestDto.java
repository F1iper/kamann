package pl.kamann.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventUpdateRequestDto(
    @NotNull Long id,
    @NotBlank String title,
    String description,
    @NotNull LocalDateTime start,
    @NotNull Integer durationMinutes,
    String rrule,
    Long instructorId,
    @NotNull Integer maxParticipants
) {}