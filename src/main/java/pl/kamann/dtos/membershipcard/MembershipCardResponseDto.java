package pl.kamann.dtos.membershipcard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kamann.membershipcard.model.MembershipCardType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCardResponseDto {

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
