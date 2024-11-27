package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.UserEventRegistrationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
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
