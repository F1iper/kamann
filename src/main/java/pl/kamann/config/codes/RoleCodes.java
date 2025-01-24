package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleCodes {
    ADMIN("ADMIN"),
    INSTRUCTOR("INSTRUCTOR"),
    CLIENT("CLIENT");

    private final String code;
}
