package pl.kamann.dtos;

import java.time.LocalDateTime;

public record OccurrenceEventLightDto(
    Long id,
    LocalDateTime start,
    LocalDateTime end,
    String title,
    String instructorName
) {}