package pl.kamann.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;

import java.time.LocalTime;

@Builder
public record FacilityDto(
    long id,

    @NotNull(message = "Name cannot be null")
    String name,

    @NotNull(message = "Address cannot be null")
    String address,

    @Schema(type = "string", format = "time", example = "08:00:00")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime openingHours,

    @Schema(type = "string", format = "time", example = "18:00:00")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime closingHours
){
    public FacilityDto {
        if (openingHours != null && closingHours != null) {
            validateOpeningAndClosingHours(openingHours, closingHours);
        }
    }

    private void validateOpeningAndClosingHours(LocalTime openingHours, LocalTime closingHours) {
        if (openingHours.isAfter(closingHours)) {
            throw new ApiException(
                    "Opening hours must be before closing hours",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_OPENING_CLOSING_HOURS"
            );
        }
    }
}
