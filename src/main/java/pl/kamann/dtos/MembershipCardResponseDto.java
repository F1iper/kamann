package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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
    private BigDecimal price;

}
