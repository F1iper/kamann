package pl.kamann.services.membershipcard.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.membershipcard.MembershipCardRequestDto;
import pl.kamann.dtos.membershipcard.MembershipCardResponseDto;
import pl.kamann.mappers.membershipcard.MembershipCardMapper;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;

    public MembershipCardResponseDto purchaseMembershipCardForClient(MembershipCardRequestDto request) {
        AppUser user = lookupService.getLoggedInUser();
        MembershipCardType type = request.getMembershipCardType();

        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(type);
        card.setEntrancesLeft(type.getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(type.getValidDays()));
        card.setPaid(false);
        card.setActive(false);

        MembershipCard savedCard = membershipCardRepository.save(card);
        return MembershipCardMapper.toResponseDto(savedCard);

    }
}
