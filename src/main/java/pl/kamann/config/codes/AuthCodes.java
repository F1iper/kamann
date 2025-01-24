package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthCodes {
    SUCCESS("SUCCESS"),
    ACCESS_DENIED("ACCESS_DENIED"),
    UNAUTHORIZED("UNAUTHORIZED"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND"),
    INVALID_ROLE("INVALID_ROLE"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS"),
    INVALID_TOKEN("INVALID_TOKEN");

    private final String code;
}
