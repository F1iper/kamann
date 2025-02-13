package pl.kamann.config.filter;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;

import java.util.Arrays;

@Getter
public enum OccurrenceFilter {
    PAST,
    UPCOMING,
    AVAILABLE;

    public static OccurrenceFilter fromString(String value) {
        return Arrays.stream(values())
                .filter(f -> f.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        "Invalid filter: " + value,
                        HttpStatus.BAD_REQUEST,
                        StatusCodes.INVALID_FILTER.name()));
    }
}