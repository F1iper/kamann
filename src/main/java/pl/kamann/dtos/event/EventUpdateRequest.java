package pl.kamann.dtos.event;

import java.time.LocalDateTime;

public record EventUpdateRequest(
        String title,
        String description,
        LocalDateTime start,
        Integer durationMinutes,
        String rrule,
        Long instructorId,
        Integer maxParticipants
) {
}
