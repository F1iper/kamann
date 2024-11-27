package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.MembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.entities.MembershipCard;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardMapper membershipCardMapper;
    private final EntityLookupService lookupService;

    public MembershipCardResponseDto purchaseMembershipCardForClient(MembershipCardRequestDto request) {
        AppUser user = lookupService.getLoggedInUser();
        MembershipCard card = membershipCardMapper.toEntity(request, user);
        MembershipCard savedCard = membershipCardRepository.save(card);
        return membershipCardMapper.toDto(savedCard);
    }
}
