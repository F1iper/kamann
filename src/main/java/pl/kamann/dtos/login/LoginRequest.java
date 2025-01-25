package pl.kamann.dtos.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email cannot be blank")
        @Schema(description = "CLIENT's email address", example = "client1@client.com")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Schema(description = "CLIENT's password", example = "admin")
        String password
) {}