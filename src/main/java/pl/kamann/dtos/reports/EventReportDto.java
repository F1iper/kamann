package pl.kamann.dtos.reports;

public record EventReportDto(
        String eventType,
        long totalEvents,
        long completedEvents,
        long cancelledEvents
) {
}
