package pl.kamann.dtos;

public record AttendanceResponseDto(
    Long attendanceId,
    Long occurrenceId,
    Long userId
) {}