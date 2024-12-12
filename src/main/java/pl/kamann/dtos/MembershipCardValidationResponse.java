package pl.kamann.dtos;

public record MembershipCardValidationResponse(
        boolean valid,
        String message,
        Long clientId,
        Long eventId,
        int remainingEntrances
) {
}