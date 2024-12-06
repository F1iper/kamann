package pl.kamann.dtos;

import lombok.Builder;
import pl.kamann.entities.attendance.AttendanceStatus;

@Builder
public record AttendanceDetailsDto(
        Long id,
        Long eventId,
        Long userId,
        AttendanceStatus status
) {
}