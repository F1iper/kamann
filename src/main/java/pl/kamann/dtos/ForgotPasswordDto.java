package pl.kamann.dtos;

import jakarta.validation.constraints.Email;

public record ForgotPasswordDto(
    @Email
    String email
) {}
