package pl.kamann.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import pl.kamann.entities.event.EventFrequency;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record EventDto(
        @NotNull(message = "Event ID cannot be null")
        Long id,

        @NotBlank(message = "Title cannot be blank")
        String title,

        String description,

        @NotNull(message = "Start date cannot be null")
        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        @NotNull(message = "End date cannot be null")
        @Future(message = "End date must be in the future")
        LocalDate endDate,

        @NotNull(message = "Start time cannot be null")
        LocalTime startTime,

        @NotNull(message = "End time cannot be null")
        LocalTime endTime,

        Boolean recurring,

        @NotNull(message = "Creator ID cannot be null")
        Long createdById,

        Long instructorId,

        List<LocalDate> exdates,

        String instructorFullName,

        @PositiveOrZero(message = "Max participants must be zero or a positive number")
        Integer maxParticipants,

        @NotNull(message = "Event status cannot be null")
        EventStatus status,

        @PositiveOrZero(message = "Current participants must be zero or a positive number")
        int currentParticipants,

        Long eventTypeId,

        String eventTypeName,

        @NotNull(message = "Frequency is required for recurring events")
        EventFrequency frequency,

        @NotEmpty(message = "Days of week are required for recurring events")
        String daysOfWeek,

        @Future(message = "Recurrence end date must be in the future")
        @NotNull(message = "Recurrence end date is required for recurring events")
        LocalDate recurrenceEndDate,

        String rrule
) {
}