package pl.kamann.mappers.membershipcard;

import pl.kamann.dtos.membershipcard.MembershipCardResponseDto;
import pl.kamann.membershipcard.model.MembershipCard;

public class MembershipCardMapper {

    public static MembershipCardResponseDto toResponseDto(MembershipCard card) {
        return new MembershipCardResponseDto(
                card.getId(),
                card.getUser().getId(),
                card.getMembershipCardType(),
                card.getEntrancesLeft(),
                card.getStartDate(),
                card.getEndDate(),
                card.isPaid(),
                card.isActive(),
                card.isPendingApproval(),
                card.getPurchaseDate()
        );
    }
}
