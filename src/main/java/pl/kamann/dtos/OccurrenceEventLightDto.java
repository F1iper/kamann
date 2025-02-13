package pl.kamann.dtos;

import java.time.LocalDateTime;

public record OccurrenceEventLightDto(
    Long occurrenceId,
    Long eventId,
    LocalDateTime start,
    LocalDateTime end,
    String title,
    String instructorName
) {}