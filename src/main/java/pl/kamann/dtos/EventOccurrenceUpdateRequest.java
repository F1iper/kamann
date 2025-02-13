package pl.kamann.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EventOccurrenceUpdateRequest {
    @NotNull(message = "New date is required")
    private LocalDate newDate;

}