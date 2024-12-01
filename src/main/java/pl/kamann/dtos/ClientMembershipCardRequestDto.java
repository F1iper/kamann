package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kamann.entities.membershipcard.MembershipCardType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientMembershipCardRequestDto {
    private MembershipCardType membershipCardType;
}