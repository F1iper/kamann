package pl.kamann.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.time.LocalDateTime;

@Builder
public record UserCardHistoryDto(
        @NotNull(message = "ID cannot be null")
        Long id,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Membership card type cannot be null")
        MembershipCardType membershipCardType,

        @NotNull(message = "Start date cannot be null")
        @FutureOrPresent(message = "Start date must be in the present or future")
        LocalDateTime startDate,

        @NotNull(message = "End date cannot be null")
        @FutureOrPresent(message = "End date must be in the present or future")
        LocalDateTime endDate,

        @PositiveOrZero(message = "Entrances must be zero or a positive number")
        int entrances,

        @PositiveOrZero(message = "Remaining entrances must be zero or a positive number")
        int remainingEntrances,

        boolean paid
) {
}
