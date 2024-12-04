package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.AdminMembershipCardRequest;
import pl.kamann.dtos.MembershipCardResponse;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.time.LocalDateTime;

@Component
public class MembershipCardMapper {

    public MembershipCardResponse toResponse(MembershipCard card) {
        return MembershipCardResponse
                .builder()
                .id(card.getId())
                .userId(card.getUser().getId())
                .membershipCardType(card.getMembershipCardType().getDisplayName())
                .entrancesLeft(card.getEntrancesLeft())
                .startDate(card.getStartDate())
                .endDate(card.getEndDate())
                .paid(card.isPaid())
                .active(card.isActive())
                .build();
    }

    public MembershipCard toEntity(AdminMembershipCardRequest dto, AppUser user) {
        return MembershipCard
                .builder()
                .user(user)
                .membershipCardType(MembershipCardType.valueOf(dto.membershipCardType()))
                .entrancesLeft(dto.entrancesLeft() != 0 ? dto.entrancesLeft() : MembershipCardType.valueOf(dto.membershipCardType()).getMaxEntrances())
                .startDate(dto.startDate() != null ? dto.startDate() : LocalDateTime.now())
                .endDate(dto.endDate() != null ? dto.endDate() : LocalDateTime.now().plusDays(MembershipCardType.valueOf(dto.membershipCardType()).getValidDays()))
                .price(dto.price())
                .paid(false)
                .active(false)
                .build();
    }
}
