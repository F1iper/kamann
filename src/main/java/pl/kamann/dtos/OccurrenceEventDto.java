package pl.kamann.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record OccurrenceEventDto(

        @NotNull(message = "Event ID cannot be null")
        Long eventId,

        @NotNull(message = "Date cannot be null")
        LocalDate date,

        @NotNull(message = "Start time cannot be null")
        LocalTime startTime,

        LocalTime endTime,

        int durationMinutes,

        boolean canceled,

        Long instructorId,

        Long createdById,

        int seriesIndex,

        int maxParticipants,

        String eventTypeName,

        String instructorFullName,

        boolean isModified,

        int attendanceCount
) {
}