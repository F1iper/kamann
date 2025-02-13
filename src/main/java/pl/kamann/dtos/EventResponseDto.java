package pl.kamann.dtos;

import java.time.LocalDateTime;

public record EventResponseDto(
    Long id,
    String title,
    String description,
    LocalDateTime start,
    Integer durationMinutes,
    String rrule,
    Long instructorId,
    Integer maxParticipants
) {}