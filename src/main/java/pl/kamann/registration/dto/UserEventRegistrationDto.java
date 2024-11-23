package pl.kamann.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.registration.model.UserEventRegistrationStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRegistrationDto {

    private Long id;
    private Long userId;
    private Long eventId;
    private UserEventRegistrationStatus status;
    private Integer waitlistPosition;
    private LocalDateTime registrationDate;
}
