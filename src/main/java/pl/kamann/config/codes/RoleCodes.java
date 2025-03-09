package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleCodes {
    CLIENT("CLIENT"),
    INSTRUCTOR("INSTRUCTOR");

    private final String code;
}
