package pl.kamann.dtos;

public record EventCancelResponse(
        Long eventId,
        String message
) {
}
