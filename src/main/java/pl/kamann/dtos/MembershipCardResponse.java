package pl.kamann.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MembershipCardResponse(
        @NotNull Long id,
        @NotNull Long userId,
        @NotNull String membershipCardType,
        int entrancesLeft,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean paid,
        boolean active
) {}
