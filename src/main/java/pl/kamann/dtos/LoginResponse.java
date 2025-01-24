package pl.kamann.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
        @NotBlank(message = "Token cannot be blank")
        String token
) {}