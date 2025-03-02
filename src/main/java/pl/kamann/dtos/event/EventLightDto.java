package pl.kamann.dtos.event;

import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

public record EventLightDto(
        Long id,
        String title,
        LocalDateTime start,
        Integer durationMinutes,
        EventStatus status,
        String eventTypeName
) {
}