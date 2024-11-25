package pl.kamann.auth.register;

import jakarta.validation.constraints.NotBlank;
import pl.kamann.auth.role.model.Role;

public record RegisterRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        Role role) {
}
