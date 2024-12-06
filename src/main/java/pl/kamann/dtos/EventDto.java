package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean recurring,
        Long createdById,
        Long instructorId,
        int maxParticipants,
        EventStatus status,
        Integer currentParticipants,
        Long eventTypeId,
        String eventTypeName
) {}