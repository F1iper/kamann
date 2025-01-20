package pl.kamann.dtos;

import lombok.Builder;
import pl.kamann.entities.appuser.AppUserStatus;

import java.time.LocalDateTime;

@Builder
public record AppUserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        AppUserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}