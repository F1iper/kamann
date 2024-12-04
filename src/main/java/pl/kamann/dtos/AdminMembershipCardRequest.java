package pl.kamann.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminMembershipCardRequest(
        @NotNull(message = "Client ID cannot be null")
        Long clientId,

        @NotNull(message = "Membership card type cannot be null")
        String membershipCardType,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Positive(message = "Entrances left must be a positive number")
        int entrancesLeft,

        LocalDateTime startDate,

        LocalDateTime endDate
) {}
