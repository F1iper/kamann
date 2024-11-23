package pl.kamann.membershipcard.dto;

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
public class MembershipCardDto {

    private Long id;
    private Long userId;
    private MembershipCardType membershipCardType;
    private int entrancesLeft;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean paid;
    private boolean active;
    private boolean pendingApproval;
    private LocalDateTime purchaseDate;
}
