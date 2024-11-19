package pl.kamann.exception.response;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String code, String message, LocalDateTime timestamp) {
}