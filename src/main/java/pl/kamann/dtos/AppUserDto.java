package pl.kamann.dtos;

import lombok.Builder;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;

import java.util.Set;

@Builder
public record AppUserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        AppUserStatus status
) {
}