package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.ClientMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.MembershipCard;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardService {

    private final MembershipCardService membershipCardService;
    private final EntityLookupService lookupService;
    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardMapper membershipCardMapper;

    public MembershipCardResponseDto getActiveCardForLoggedInUser() {
        AppUser user = lookupService.getLoggedInUser();
        MembershipCard activeCard = membershipCardService.findActiveCardByUserId(user.getId());
        return membershipCardMapper.toDto(activeCard);
    }

    public MembershipCardResponseDto purchaseMembershipCardForClient(ClientMembershipCardRequestDto request) {
        AppUser user = lookupService.getLoggedInUser();

        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(request.getMembershipCardType());
        card.setEntrancesLeft(request.getMembershipCardType().getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(request.getMembershipCardType().getValidDays()));
        card.setPaid(false);
        card.setActive(false);

        MembershipCard savedCard = membershipCardRepository.save(card);
        return membershipCardMapper.toDto(savedCard);
    }
}
