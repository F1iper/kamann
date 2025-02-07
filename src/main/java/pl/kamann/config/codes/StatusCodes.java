package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodes {
    INVALID_REQUEST("INVALID_REQUEST"),
    INVALID_INPUT("INVALID_INPUT"),
    NO_RESULTS("NO_RESULTS"),
    INVALID_FILTER("INVALID_FILTER");

    private final String code;
}
