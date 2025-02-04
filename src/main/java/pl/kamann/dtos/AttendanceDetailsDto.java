package pl.kamann.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import pl.kamann.entities.attendance.AttendanceStatus;

@Builder
public record AttendanceDetailsDto(
        @NotNull(message = "Attendance ID cannot be null")
        Long id,

        @NotNull(message = "Event ID cannot be null")
        Long occurrenceEventId,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Attendance status cannot be null")
        AttendanceStatus status
) {
}
