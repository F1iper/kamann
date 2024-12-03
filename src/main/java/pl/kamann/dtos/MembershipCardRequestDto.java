package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCardRequestDto {

    private MembershipCardType membershipCardType;
    private BigDecimal price;

}
