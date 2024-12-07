package pl.kamann.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MembershipCardResponse(
        @NotNull(message = "Membership card ID cannot be null")
        Long id,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotBlank(message = "Membership card type cannot be blank")
        String membershipCardType,

        @PositiveOrZero(message = "Entrances left must be zero or a positive number")
        int entrancesLeft,

        @NotNull(message = "Start date cannot be null")
        @FutureOrPresent(message = "Start date must be in the present or future")
        LocalDateTime startDate,

        @NotNull(message = "End date cannot be null")
        @FutureOrPresent(message = "End date must be in the present or future")
        LocalDateTime endDate,

        boolean paid,

        boolean active
) {}
