package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthCodes {
    UNAUTHORIZED("UNAUTHORIZED"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS");

    private final String code;
}
