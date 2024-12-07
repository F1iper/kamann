package pl.kamann.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;

import java.util.Set;

@Builder
public record AppUserDto(
        @NotNull(message = "User ID cannot be null")
        Long id,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        String lastName,

        @NotNull(message = "Roles cannot be null")
        Set<Role> roles,

        @NotNull(message = "User status cannot be null")
        AppUserStatus status
) {
}