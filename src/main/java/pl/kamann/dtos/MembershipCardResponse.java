package pl.kamann.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MembershipCardResponse(
        Long id,
        Long userId,
        String membershipCardType,
        int entrancesLeft,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean paid,
        boolean active
) {}