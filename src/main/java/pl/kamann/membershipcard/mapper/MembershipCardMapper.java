package pl.kamann.membershipcard.mapper;

import org.springframework.stereotype.Component;
import pl.kamann.membershipcard.dto.MembershipCardDto;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.user.model.AppUser;

@Component
public class MembershipCardMapper {

    public MembershipCardDto toDto(MembershipCard card) {
        return MembershipCardDto.builder()
                .id(card.getId())
                .userId(card.getUser().getId())
                .membershipCardType(card.getMembershipCardType())
                .entrancesLeft(card.getEntrancesLeft())
                .startDate(card.getStartDate())
                .endDate(card.getEndDate())
                .paid(card.isPaid())
                .active(card.isActive())
                .pendingApproval(card.isPendingApproval())
                .purchaseDate(card.getPurchaseDate())
                .build();
    }

    public MembershipCard toEntity(MembershipCardDto dto, AppUser user) {
        MembershipCard card = new MembershipCard();
        card.setId(dto.getId());
        card.setUser(user);
        card.setMembershipCardType(dto.getMembershipCardType());
        card.setEntrancesLeft(dto.getEntrancesLeft());
        card.setStartDate(dto.getStartDate());
        card.setEndDate(dto.getEndDate());
        card.setPaid(dto.isPaid());
        card.setActive(dto.isActive());
        card.setPendingApproval(dto.isPendingApproval());
        card.setPurchaseDate(dto.getPurchaseDate());
        return card;
    }
}
