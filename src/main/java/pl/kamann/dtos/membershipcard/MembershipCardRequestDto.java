package pl.kamann.dtos.membershipcard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kamann.membershipcard.model.MembershipCardType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCardRequestDto {

    private MembershipCardType membershipCardType;

}
