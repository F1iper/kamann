package pl.kamann.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.membershipcard.model.MembershipCardType;

import java.time.LocalDateTime;

@Data
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
