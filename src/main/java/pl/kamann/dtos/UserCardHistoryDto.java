package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCardHistoryDto {

    private Long id;
    private Long userId;
    private MembershipCardType membershipCardType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int entrances;
    private int remainingEntrances;
    private boolean paid;
}
