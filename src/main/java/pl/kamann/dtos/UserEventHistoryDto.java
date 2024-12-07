package pl.kamann.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.kamann.entities.attendance.AttendanceStatus;

import java.time.LocalDateTime;

@Builder
public record UserEventHistoryDto(
        @NotNull(message = "ID cannot be null")
        Long id,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Event ID cannot be null")
        Long eventId,

        @NotNull(message = "Attendance status cannot be null")
        AttendanceStatus status,

        LocalDateTime attendedDate,

        @PositiveOrZero(message = "Entrances used must be zero or a positive number")
        int entrancesUsed
) {
}
