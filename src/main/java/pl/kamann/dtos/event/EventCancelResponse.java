package pl.kamann.dtos.event;

public record EventCancelResponse(
        Long eventId,
        String message
) {
}
