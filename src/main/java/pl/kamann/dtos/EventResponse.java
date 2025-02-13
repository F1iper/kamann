package pl.kamann.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponse(
    Long id,
    String title,
    String description,
    LocalDateTime start,
    Integer durationMinutes,
    String rrule,
    Long instructorId,
    Integer maxParticipants
) {}