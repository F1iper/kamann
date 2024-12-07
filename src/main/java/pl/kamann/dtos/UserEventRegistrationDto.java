package pl.kamann.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.kamann.entities.event.UserEventRegistrationStatus;

import java.time.LocalDateTime;

@Builder
public record UserEventRegistrationDto(
        @NotNull(message = "ID cannot be null")
        Long id,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Event ID cannot be null")
        Long eventId,

        @NotNull(message = "Registration status cannot be null")
        UserEventRegistrationStatus status,

        @PositiveOrZero(message = "Waitlist position must be zero or a positive number")
        Integer waitlistPosition,

        @NotNull(message = "Registration date cannot be null")
        @FutureOrPresent(message = "Registration date must be in the present or future")
        LocalDateTime registrationDate
) {}
