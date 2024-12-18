package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InstructorCodes {
    INSTRUCTOR_BUSY("INSTRUCTOR_BUSY"),
    INSTRUCTOR_NOT_FOUND("INSTRUCTOR_NOT_FOUND"),
    REGISTRATION_NOT_FOUND("REGISTRATION_NOT_FOUND");

    private final String code;
}
