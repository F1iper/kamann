package pl.kamann.dtos.reports;

public record AttendanceReportDto(
        String eventName,
        long totalParticipants,
        long attended,
        long absent,
        long lateCancellations
) {
}
