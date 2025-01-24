package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodes {
    INVALID_ATTENDANCE_STATUS("INVALID_STATUS"),
    INVALID_USER_STATUS("INVALID_USER_STATUS"),
    INVALID_STATUS_CHANGE("INVALID_STATUS_CHANGE"),
    INVALID_REQUEST("INVALID_REQUEST"),
    INVALID_INPUT("INVALID_INPUT"),
    NO_RESULTS("NO_RESULTS");

    private final String code;
}
