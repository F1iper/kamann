package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.repositories.MembershipCardRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipCardExpirationService {

    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardService membershipCardService;

    @Transactional
    public void expireMembershipCards() {
        List<MembershipCard> expiredCards = membershipCardRepository.findByEndDateBeforeAndActiveTrue(LocalDateTime.now());

        expiredCards.forEach(membershipCardService::expireCard);
    }
}