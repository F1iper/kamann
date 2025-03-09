package pl.kamann.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppUserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}