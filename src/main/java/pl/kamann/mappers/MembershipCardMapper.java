package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.MembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.entities.MembershipCard;
import pl.kamann.entities.AppUser;

import java.time.LocalDateTime;

@Component
public class MembershipCardMapper {

    public MembershipCardResponseDto toDto(MembershipCard card) {
        return MembershipCardResponseDto.builder()
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
                .price(card.getPrice())
                .build();
    }

    public MembershipCard toEntity(MembershipCardRequestDto dto, AppUser user) {
        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(dto.getMembershipCardType());
        card.setEntrancesLeft(dto.getMembershipCardType().getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(dto.getMembershipCardType().getValidDays()));
        card.setPaid(false);
        card.setActive(false);
        card.setPrice(dto.getPrice());
        return card;
    }
}